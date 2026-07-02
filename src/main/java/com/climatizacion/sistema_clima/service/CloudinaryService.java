package com.climatizacion.sistema_clima.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    public String subirImagen(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    public String subirImagenBase64(String base64String) throws IOException {
        // 1. Limpiar la cadena: eliminar prefijo "data:image/png;base64," si existe
        String cleanBase64 = base64String;
        if (base64String.contains(",")) {
            cleanBase64 = base64String.substring(base64String.indexOf(",") + 1);
        }

        // 2. Eliminar espacios en blanco y saltos de línea (por si acaso)
        cleanBase64 = cleanBase64.replaceAll("\\s", "");

        // 3. Subir a Cloudinary (Cloudinary acepta Base64 sin prefijo)
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                "data:image/png;base64," + cleanBase64,  // Reconstruir con el prefijo correcto
                ObjectUtils.emptyMap()
        );
        return uploadResult.get("secure_url").toString();
    }
}