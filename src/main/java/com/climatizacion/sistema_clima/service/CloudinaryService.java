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

    // Inyectamos la URL de Cloudinary (que puede venir de variable de entorno)
    public CloudinaryService(@Value("${cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    /**
     * Sube un archivo a Cloudinary y devuelve la URL pública segura (https)
     */
    public String subirImagen(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    public String subirImagenBase64(String base64String) throws IOException {
        // Cloudinary puede subir imágenes directamente desde una cadena Data URI de Base64
        Map<?, ?> uploadResult = cloudinary.uploader().upload(base64String, ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }
}