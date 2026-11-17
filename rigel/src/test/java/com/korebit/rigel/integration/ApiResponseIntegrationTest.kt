package com.korebit.rigel.integration

import com.jayway.jsonpath.JsonPath
import com.korebit.rigel.enums.Role
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

/**
 * Verifies the HTTP contract of the refactored controllers: 201 + resource on create,
 * 200 + resource on update, 204 on delete, and the uniform JSON error body.
 */
@DisplayName("Controller response contract (H2 + MockMvc)")
class ApiResponseIntegrationTest : IntegrationTestSupport() {

    private fun supplierJson(name: String): String =
        json("name", name, "contactEmail", "contact@$name.test", "phoneNumber", "5550000", "address", "Calle 1")

    private fun productJson(name: String, barcode: String, supplier: String, batchCode: String): String {
        val today = LocalDate.now()
        return """
            {"name":"$name","description":"desc","barcode":"$barcode","category":"SODA","price":20.0,
             "supplierName":"$supplier","supplierPrice":12.5,"minStock":3,
             "batch":{"code":"$batchCode","receptionDate":"$today","expirationDate":"${today.plusDays(30)}","receivedAmount":10,
                      "price":12.5,"available":true,"notes":""}}
        """.trimIndent()
    }

    @Test
    @DisplayName("Supplier CRUD: 201 → 200 → 200 → 204 → 404")
    fun supplierLifecycle() {
        val root = loginAsRoot()
        val name = unique("sup")

        performJson(post("/suppliers/add"), root, supplierJson(name))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(name))
            .andExpect(jsonPath("$.status").doesNotExist())
            .andExpect(jsonPath("$.success").doesNotExist())

        perform(get("/suppliers/$name"), root).andExpect(status().isOk()).andExpect(jsonPath("$.name").value(name))
        perform(get("/suppliers/$name/products"), root)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize<Any>(0)))

        val renamed = "$name-v2"
        performJson(put("/suppliers/$name"), root, supplierJson(renamed))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(renamed))

        perform(delete("/suppliers/$renamed"), root)
            .andExpect(status().isNoContent())
            .andExpect(content().string(""))

        perform(get("/suppliers/$renamed"), root)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message", containsString(renamed)))
    }

    @Test
    @DisplayName("Validation errors are 400 and duplicates do not create a second row")
    fun validationErrors() {
        val root = loginAsRoot()
        val name = unique("dup")
        performJson(post("/suppliers/add"), root, supplierJson(name)).andExpect(status().isCreated())

        performJson(post("/suppliers/add"), root, supplierJson(name))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("already exists")))

        performJson(post("/suppliers/add"), root, "{not json")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    @DisplayName("Product + POS sale: stock is consumed, the seller is the authenticated user, delete restores stock")
    fun productAndTicketFlow() {
        val root = loginAsRoot()
        val supplier = unique("sup")
        val product = unique("prod")
        val barcode = unique("bc")
        performJson(post("/suppliers/add"), root, supplierJson(supplier)).andExpect(status().isCreated())

        performJson(post("/product/add"), root, productJson(product, barcode, supplier, unique("lot")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(product))
            .andExpect(jsonPath("$.stock").value(10))
            .andExpect(jsonPath("$.minStock").value(3))

        performJson(patch("/product/$barcode/min-stock"), root, """{"minStock":5}""")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.minStock").value(5))

        val seller = createUser(Role.USER)
        val user = login(seller, "secret123")
        val ticketBody = """
            {"description":"venta","dateAndTime":"${LocalDate.now()}","methodPayment":"CARD",
             "currentConsumerEmail":"$ROOT_EMAIL",
             "products":[{"barcode":"$barcode","quantity":4,"price":20.0,"discount":5.0}]}
        """.trimIndent()

        val ticket = performJson(post("/tickets"), user, ticketBody)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.consumer").value(seller))
            .andExpect(jsonPath("$.totalAmount").value(75.0))
            .andExpect(jsonPath("$.payment").value("CARD"))
            .andReturn().response.contentAsString
        val ticketBarcode: String = JsonPath.read(ticket, "$.barcode")

        perform(get("/product/$product"), root).andExpect(jsonPath("$.stock").value(6))

        // USER may sell but not delete tickets
        perform(delete("/tickets/$ticketBarcode"), user).andExpect(status().isForbidden())
        perform(delete("/tickets/$ticketBarcode"), root).andExpect(status().isNoContent())
        perform(get("/product/$product"), root).andExpect(jsonPath("$.stock").value(10))

        perform(delete("/product/$product"), root).andExpect(status().isNoContent())
        perform(get("/product/$product"), root).andExpect(status().isNotFound())
    }

    @Test
    @DisplayName("Selling more than the available stock is a 400 and does not touch inventory")
    fun insufficientStock() {
        val root = loginAsRoot()
        val supplier = unique("sup")
        val product = unique("prod")
        val barcode = unique("bc")
        performJson(post("/suppliers/add"), root, supplierJson(supplier)).andExpect(status().isCreated())
        performJson(post("/product/add"), root, productJson(product, barcode, supplier, unique("lot")))
            .andExpect(status().isCreated())

        val ticketBody = """
            {"description":"","dateAndTime":"${LocalDate.now()}","methodPayment":"CASH",
             "products":[{"barcode":"$barcode","quantity":11,"price":20.0,"discount":0}]}
        """.trimIndent()

        performJson(post("/tickets"), root, ticketBody)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Insufficient")))
        perform(get("/product/$product"), root).andExpect(jsonPath("$.stock").value(10))
    }

    @Test
    @DisplayName("User administration returns the user DTO without password")
    fun userAdministration() {
        val root = loginAsRoot()
        val username = unique("adm")
        val body = """
            {"name":"Nuevo","username":"$username","password":"secret123","email":"$username@test.local",
             "phoneNumber":"","role":"ADMIN"}
        """.trimIndent()

        val created = performJson(post("/consumer/api/users"), root, body)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andReturn().response.contentAsString
        val id: Int = JsonPath.read(created, "$.id")

        performJson(patch("/consumer/api/users/$id/status"), root, """{"active":false}""")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false))

        perform(get("/consumer/api/all"), root)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].password").doesNotExist())
    }
}
