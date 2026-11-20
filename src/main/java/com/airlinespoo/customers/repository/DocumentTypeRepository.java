package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.customers.model.DocumentType;

public class DocumentTypeRepository extends InMemoryRepository<DocumentType, String> {

    @Override
    protected String entityName() {
        return "tipo de documento";
    }
}