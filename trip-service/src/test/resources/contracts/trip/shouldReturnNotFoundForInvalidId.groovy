package contracts.trip

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return 404 for invalid trip ID"
    
    request {
        method GET()
        url '/api/v1/trips/999'
        headers {
            accept('application/json')
        }
    }
    
    response {
        status 404
        headers {
            contentType('application/json')
        }
    }
}

