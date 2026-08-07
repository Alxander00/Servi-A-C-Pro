package com.climatizacion.sistema_clima.specification;

import com.climatizacion.sistema_clima.entities.ProductoEntity;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ProductoSpecification {

    public static Specification<ProductoEntity> conFiltros(
            String busqueda,
            String categoria,
            String marca,
            Double precioMin,
            Double precioMax,
            Integer btuMin,
            Integer btuMax) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre filtrar activos
            predicates.add(cb.isTrue(root.get("activo")));

            // Búsqueda por nombre o descripción
            if (busqueda != null && !busqueda.isEmpty()) {
                String likePattern = "%" + busqueda.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), likePattern),
                        cb.like(cb.lower(root.get("descripcion")), likePattern)
                ));
            }

            // Categoría por nombre (si existe)
            if (categoria != null && !categoria.isEmpty() && !categoria.equals("todas")) {
                predicates.add(cb.equal(cb.lower(root.get("categoria").get("nombre")), categoria.toLowerCase()));
            }

            // Marca
            if (marca != null && !marca.isEmpty() && !marca.equals("todas")) {
                predicates.add(cb.equal(cb.lower(root.get("marca")), marca.toLowerCase()));
            }

            // Precio
            if (precioMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), precioMin));
            }
            if (precioMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
            }

            // BTU
            if (btuMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("capacidadBtu"), btuMin.longValue()));
            }
            if (btuMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("capacidadBtu"), btuMax.longValue()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}