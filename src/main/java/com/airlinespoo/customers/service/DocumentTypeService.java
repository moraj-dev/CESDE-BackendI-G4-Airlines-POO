package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.customers.repository.DocumentTypeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DocumentTypeService {

    private final DocumentTypeRepository documentTypes;
    private final CustomerRepository customers;

    public DocumentTypeService(
            DocumentTypeRepository documentTypes,
            CustomerRepository customers
    ) {
        this.documentTypes = Validator.requireNotNull(
                documentTypes, "repositorio de tipos de documento"
        );
        this.customers = Validator.requireNotNull(
                customers, "repositorio de clientes"
        );
    }

    public DocumentType createDocumentType(String id, String description) {
        return documentTypes.create(new DocumentType(id, description));
    }

    public DocumentType getDocumentType(String id) {
        String key = Validator.requireText(
                id, "código del tipo de documento"
        ).toUpperCase(Locale.ROOT);

        return documentTypes.findById(key)
                .orElseThrow(() -> new NotFoundException(
                        "No existe el tipo de documento '" + key + "'."
                ));
    }

    public List<DocumentType> listDocumentTypes() {
        return documentTypes.findAll().stream()
                .sorted(Comparator.comparing(
                        DocumentType::getDescription,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .toList();
    }

    public DocumentType updateDocumentType(String id, String description) {
        DocumentType type = getDocumentType(id);
        type.setDescription(description);
        return documentTypes.update(type);
    }

    public void deleteDocumentType(String id) {
        DocumentType type = getDocumentType(id);

        if (customers.existsByDocumentTypeId(type.getId())) {
            throw new BusinessRuleException(
                    "No se puede eliminar el tipo porque está asociado a clientes."
            );
        }

        documentTypes.deleteById(type.getId());
    }

    public long count() {
        return documentTypes.count();
    }
}