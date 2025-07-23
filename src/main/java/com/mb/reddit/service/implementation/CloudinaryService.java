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
    private static final int MAX_WIDTH = 792;
    private static final int MAX_HEIGHT = 500;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        System.out.println("Original File size: " + (file.getSize() / 1024) + " KB");
        System.out.println("Content Type: " + contentType);

        byte[] processedImage;

        if(contentType != null && contentType.equalsIgnoreCase("image/svg+xml")) {
            processedImage = file.getBytes();
        }
        else {
            processedImage = resizeAndCompress(file);
        }

        System.out.println("Processed File size: " + (processedImage.length / 1024) + " KB");

        Map uploadResult = cloudinary.uploader().upload(processedImage, ObjectUtils.asMap("resource_type", "auto"));
        return uploadResult.get("secure_url").toString();
    }

    private byte[] resizeAndCompress(MultipartFile file) throws IOException {
        long originalSizeKB = file.getSize() / 1024;

        BufferedImage image = ImageIO.read(file.getInputStream());
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        // Resize if dimensions exceed limits
        int targetWidth = originalWidth;
        int targetHeight = originalHeight;
        if(originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
            float widthRatio = (float) MAX_WIDTH / originalWidth;
            float heightRatio = (float) MAX_HEIGHT / originalHeight;
            float ratio = Math.min(widthRatio, heightRatio);
            targetWidth = Math.round(originalWidth * ratio);
            targetHeight = Math.round(originalHeight * ratio);
        }

        String format = "jpg";
        String contentType = file.getContentType();
        if(contentType != null) {
            if(contentType.contains("webp")) {
                format = "webp";
            }
            else if(contentType.contains("png") && originalSizeKB < 300) {
                format = "png";
            }
        }

        if(originalSizeKB < 20 && targetWidth == originalWidth && targetHeight == originalHeight) {
            return file.getBytes();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        float quality = 0.8f;
        Thumbnails.of(image).size(targetWidth, targetHeight).outputFormat(format).outputQuality(quality).toOutputStream(outputStream);

        while(outputStream.size() / 1024 > 500 && quality > 0.5f) {
            quality -= 0.1f;
            outputStream.reset();
            Thumbnails.of(image).size(targetWidth, targetHeight).outputFormat(format).outputQuality(quality).toOutputStream(outputStream);
        }

        return outputStream.toByteArray();
    }
}
