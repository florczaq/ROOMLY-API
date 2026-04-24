package org.roomly.dto;

import org.roomly.annotations.ValidBarcode;

public record ProductDTO(int id, @ValidBarcode String barcode, String name, String brand, String quantity) {
}
