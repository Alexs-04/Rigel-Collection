/**
 * Provides the business service layer for Rigel.
 *
 * <p>This package orchestrates application use cases, validates inputs, enforces business
 * rules, and coordinates repository access under transactional boundaries. Services in this
 * package are the bridge between REST controllers and persistence components.
 *
 * <p>Main responsibilities include:
 *
 * <ul>
 *   <li>Authentication and authorization flows</li>
 *   <li>Product, supplier, batch, and purchase lifecycle management</li>
 *   <li>Point-of-sale ticket operations and payment registration</li>
 *   <li>Dashboard metrics aggregation and system movement tracking</li>
 * </ul>
 *
 * <p>Implementations should keep controllers thin, encapsulate domain logic, and provide
 * consistent error handling for the API layer.
 */
package com.korebit.rigel.service;
