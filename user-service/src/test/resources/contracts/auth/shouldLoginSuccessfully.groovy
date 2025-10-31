package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should login successfully with valid credentials"
    
    request {
        method POST()
        url '/api/v1/auth/login'
        headers {
            contentType('application/json')
        }
        body([
            email: "test@example.com",
            password: "Password123!"
        ])
    }
    
    response {
        status 200
        headers {
            contentType('application/json')
        }
        body([
            token: $(consumer(regex('[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+')), producer('eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIn0.token')),
            userId: 1,
            email: "test@example.com",
            firstName: "Test",
            lastName: "User",
            roles: ["TRAVELER"]
        ])
    }
}

