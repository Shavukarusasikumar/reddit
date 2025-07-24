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
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Image resizing dimensions
    private static final int MAX_WIDTH = 792;
    private static final int MAX_HEIGHT = 500;

    private static final long MAX_IMAGE_SIZE = 20L * 1024 * 1024;      // 10 MB

    private static final long MAX_VIDEO_SIZE = 50L * 1024 * 1024;      // 30 MB


    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadMedia(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        if(contentType == null) {
            throw new IOException("File has no content type.");
        }
        System.out.println("File type: " + contentType);
        System.out.println("File size: " + (fileSize / 1024) + " KB");

        Map<?, ?> uploadResult;

        if(contentType.startsWith("image/")) {
            validateImage(fileSize, contentType);
            byte[] imageData = contentType.equalsIgnoreCase("image/svg+xml") ? file.getBytes() : resizeWithoutLoss(file);

            uploadResult = cloudinary.uploader().upload(imageData, ObjectUtils.asMap("resource_type", "image", "format", "webp", "quality", "auto:best"));

        }
        else if(contentType.startsWith("video/")) {
            validateVideo(fileSize, contentType);

            uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "video", "format", "mp4",                  // Convert to mp4
                    "quality", "auto",                // Auto bitrate optimization
                    "eager", List.of(new com.cloudinary.Transformation().quality("auto").fetchFormat("mp4"))

            ));

        }
        else {
            throw new IOException("Unsupported media type: " + contentType);
        }

        return uploadResult.get("secure_url").toString();
    }

    private byte[] resizeWithoutLoss(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());

        if(image == null) {
            throw new IOException("Invalid image file.");
        }

        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        long originalSizeKB = file.getSize() / 1024;

        int targetWidth = originalWidth;
        int targetHeight = originalHeight;

        if(originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
            float widthRatio = (float) MAX_WIDTH / originalWidth;
            float heightRatio = (float) MAX_HEIGHT / originalHeight;
            float ratio = Math.min(widthRatio, heightRatio);
            targetWidth = Math.round(originalWidth * ratio);
            targetHeight = Math.round(originalHeight * ratio);
        }

        if(originalSizeKB < 100 && targetWidth == originalWidth && targetHeight == originalHeight) {
            return file.getBytes(); // Already optimized
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(image)
                .size(targetWidth, targetHeight)
                .outputFormat("jpg")
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    private void validateImage(long fileSize, String contentType) throws IOException {
        if(!contentType.startsWith("image/")) {
            throw new IOException("Invalid image format.");
        }

        if(fileSize > MAX_IMAGE_SIZE) {
            throw new IOException("Image is too large. Must be under 5 MB.");
        }
    }

    private void validateVideo(long fileSize, String contentType) throws IOException {
        if(!contentType.startsWith("video/")) {
            throw new IOException("Invalid video format.");
        }

        if(fileSize > MAX_VIDEO_SIZE) {
            throw new IOException("Video is too large. Must be under 50 MB.");
        }
    }
}
