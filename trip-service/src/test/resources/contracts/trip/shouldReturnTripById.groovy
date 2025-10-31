package contracts.trip

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return trip by ID"
    
    request {
        method GET()
        url '/api/v1/trips/1'
        headers {
            accept('application/json')
        }
    }
    
    response {
        status 200
        headers {
            contentType('application/json')
        }
        body([
            id: 1,
            title: "Amazing Trip to Paris",
            origin: "Moscow",
            destination: "Paris",
            price: 50000.00,
            currency: "RUB",
            availableSeats: 20,
            totalSeats: 50,
            status: "AVAILABLE"
        ])
    }
}

