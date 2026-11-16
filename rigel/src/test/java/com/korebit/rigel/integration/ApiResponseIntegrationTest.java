package com.korebit.rigel.integration;

import com.jayway.jsonpath.JsonPath;
import com.korebit.rigel.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the HTTP contract of the refactored controllers: 201 + resource on create,
 * 200 + resource on update, 204 on delete, and the uniform JSON error body.
 */
@DisplayName("Controller response contract (H2 + MockMvc)")
class ApiResponseIntegrationTest extends IntegrationTestSupport {

    private String supplierJson(String name) {
        return json("name", name, "contactEmail", "contact@" + name + ".test", "phoneNumber", "5550000", "address", "Calle 1");
    }

    private String productJson(String name, String barcode, String supplier, String batchCode) {
        LocalDate today = LocalDate.now();
        return """
                {"name":"%s","description":"desc","barcode":"%s","category":"SODA","price":20.0,
                 "supplierName":"%s","supplierPrice":12.5,"minStock":3,
                 "batch":{"code":"%s","receptionDate":"%s","expirationDate":"%s","receivedAmount":10,
                          "price":12.5,"available":true,"notes":""}}
                """.formatted(name, barcode, supplier, batchCode, today, today.plusDays(30));
    }

    @Test
    @DisplayName("Supplier CRUD: 201 → 200 → 200 → 204 → 404")
    void supplierLifecycle() throws Exception {
        Tokens root = loginAsRoot();
        String name = unique("sup");

        performJson(post("/suppliers/add"), root, supplierJson(name))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.success").doesNotExist());

        perform(get("/suppliers/" + name), root).andExpect(status().isOk()).andExpect(jsonPath("$.name").value(name));
        perform(get("/suppliers/" + name + "/products"), root).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));

        String renamed = name + "-v2";
        performJson(put("/suppliers/" + name), root, supplierJson(renamed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(renamed));

        perform(delete("/suppliers/" + renamed), root)
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        perform(get("/suppliers/" + renamed), root)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString(renamed)));
    }

    @Test
    @DisplayName("Validation errors are 400 and duplicates do not create a second row")
    void validationErrors() throws Exception {
        Tokens root = loginAsRoot();
        String name = unique("dup");
        performJson(post("/suppliers/add"), root, supplierJson(name)).andExpect(status().isCreated());

        performJson(post("/suppliers/add"), root, supplierJson(name))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already exists")));

        performJson(post("/suppliers/add"), root, "{not json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Product + POS sale: stock is consumed, the seller is the authenticated user, delete restores stock")
    void productAndTicketFlow() throws Exception {
        Tokens root = loginAsRoot();
        String supplier = unique("sup");
        String product = unique("prod");
        String barcode = unique("bc");
        performJson(post("/suppliers/add"), root, supplierJson(supplier)).andExpect(status().isCreated());

        performJson(post("/product/add"), root, productJson(product, barcode, supplier, unique("lot")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(product))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.minStock").value(3));

        performJson(patch("/product/" + barcode + "/min-stock"), root, "{\"minStock\":5}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minStock").value(5));

        String seller = createUser(Role.USER);
        Tokens user = login(seller, "secret123");
        String ticketBody = """
                {"description":"venta","dateAndTime":"%s","methodPayment":"CARD",
                 "currentConsumerEmail":"%s",
                 "products":[{"barcode":"%s","quantity":4,"price":20.0,"discount":5.0}]}
                """.formatted(LocalDate.now(), ROOT_EMAIL, barcode);

        String ticket = performJson(post("/tickets"), user, ticketBody)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consumer").value(seller))
                .andExpect(jsonPath("$.totalAmount").value(75.0))
                .andExpect(jsonPath("$.payment").value("CARD"))
                .andReturn().getResponse().getContentAsString();
        String ticketBarcode = JsonPath.read(ticket, "$.barcode");

        perform(get("/product/" + product), root).andExpect(jsonPath("$.stock").value(6));

        // USER may sell but not delete tickets
        perform(delete("/tickets/" + ticketBarcode), user).andExpect(status().isForbidden());
        perform(delete("/tickets/" + ticketBarcode), root).andExpect(status().isNoContent());
        perform(get("/product/" + product), root).andExpect(jsonPath("$.stock").value(10));

        perform(delete("/product/" + product), root).andExpect(status().isNoContent());
        perform(get("/product/" + product), root).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Selling more than the available stock is a 400 and does not touch inventory")
    void insufficientStock() throws Exception {
        Tokens root = loginAsRoot();
        String supplier = unique("sup");
        String product = unique("prod");
        String barcode = unique("bc");
        performJson(post("/suppliers/add"), root, supplierJson(supplier)).andExpect(status().isCreated());
        performJson(post("/product/add"), root, productJson(product, barcode, supplier, unique("lot")))
                .andExpect(status().isCreated());

        String ticketBody = """
                {"description":"","dateAndTime":"%s","methodPayment":"CASH",
                 "products":[{"barcode":"%s","quantity":11,"price":20.0,"discount":0}]}
                """.formatted(LocalDate.now(), barcode);

        performJson(post("/tickets"), root, ticketBody)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Insufficient")));
        perform(get("/product/" + product), root).andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    @DisplayName("User administration returns the user DTO without password")
    void userAdministration() throws Exception {
        Tokens root = loginAsRoot();
        String username = unique("adm");
        String body = """
                {"name":"Nuevo","username":"%s","password":"secret123","email":"%s@test.local",
                 "phoneNumber":"","role":"ADMIN"}
                """.formatted(username, username);

        String created = performJson(post("/consumer/api/users"), root, body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(created, "$.id");

        performJson(patch("/consumer/api/users/" + id + "/status"), root, "{\"active\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        perform(get("/consumer/api/all"), root)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }
}
