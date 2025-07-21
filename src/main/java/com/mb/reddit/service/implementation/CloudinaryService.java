package com.mb.reddit.service.implementation;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType(); // e.g., "image/png", "image/jpeg", "image/webp", "image/svg+xml"
        System.out.println("Original File size: " + (file.getSize() / 1024) + " KB");
        System.out.println("Content Type: " + contentType);

        byte[] processedImage;

        if(contentType.equalsIgnoreCase("image/svg+xml")) {
            processedImage = file.getBytes();
        }
        else if(contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/jpg")) {
            processedImage = compressImage(file, "jpg");
        }
        else if(contentType.equalsIgnoreCase("image/png")) {
            processedImage = convertToJpgAndCompress(file);
        }
        else if(contentType.equalsIgnoreCase("image/webp")) {
            processedImage = file.getBytes();
        }
        else {
            processedImage = compressImage(file, "jpg");
        }

        System.out.println("Processed File size: " + (processedImage.length / 1024) + " KB");


        Map uploadResult = cloudinary.uploader().upload(processedImage, ObjectUtils.asMap("resource_type", "auto"));
        return uploadResult.get("secure_url").toString();
    }

    private byte[] compressImage(MultipartFile file, String format) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        int targetWidth = Math.min(1920, originalWidth);
        int targetHeight = Math.min(1080, originalHeight);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(image)
                .size(targetWidth, targetHeight)
                .outputQuality(0.7f)
                .outputFormat(format)
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    private byte[] convertToJpgAndCompress(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(image).size(1920, 1080).outputQuality(0.7f).outputFormat("jpg").toOutputStream(outputStream);
        return outputStream.toByteArray();
    }
}
