# Package com.korebit.rigel.service

Provides the business service layer for Rigel.

This package orchestrates application use cases, validates inputs, enforces business
rules, and coordinates repository access under transactional boundaries. Services in this
package are the bridge between REST controllers and persistence components.

Main responsibilities include:

- Authentication and authorization flows
- Product, supplier, batch, and purchase lifecycle management
- Point-of-sale ticket operations and payment registration
- Dashboard metrics aggregation and system movement tracking

Implementations should keep controllers thin, encapsulate domain logic, and provide
consistent error handling for the API layer.

# Package com.korebit.rigel.util

Utility components for authentication, token handling, and startup initialization.

This package contains cross-cutting infrastructure utilities used by the
security layer and application bootstrap process.
