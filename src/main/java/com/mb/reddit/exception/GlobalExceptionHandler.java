package com.mb.reddit.exception;

import com.mb.reddit.exception.custom.*;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException userNotFoundException, Model model) {
        model.addAttribute("message", userNotFoundException.getMessage());
        model.addAttribute("title", HttpStatus.NOT_FOUND);

        return "error";
    }

    @ExceptionHandler(PostNotFoundException.class)
    public String handlePostNotFoundException(PostNotFoundException postNotFoundException, Model model) {
        model.addAttribute("message", postNotFoundException.getMessage());
        model.addAttribute("title", HttpStatus.NOT_FOUND);

        return "error";
    }

    @ExceptionHandler(CommunityNotFoundException.class)
    public String handleCommunityNotFoundException(CommunityNotFoundException communityNotFoundException, Model model) {
        model.addAttribute("message", communityNotFoundException.getMessage());
        model.addAttribute("title", HttpStatus.NOT_FOUND);

        return "error";
    }

    @ExceptionHandler(MediaUploadError.class)
    public String handleMediaUploadError(MediaUploadError mediaUploadError, Model model) {
        model.addAttribute("message", mediaUploadError.getMessage());
        model.addAttribute("title", HttpStatus.INTERNAL_SERVER_ERROR);

        return "error";
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public String handleCommentNotFoundException(CommentNotFoundException commentNotFoundException, Model model) {
        model.addAttribute("message", commentNotFoundException.getMessage());
        model.addAttribute("title", HttpStatus.NOT_FOUND);

        return "error";
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public String handleUnauthorizedAccessException(UnauthorizedAccessException unauthorizedAccessException, Model model) {
        model.addAttribute("message", unauthorizedAccessException.getMessage());
        model.addAttribute("title", HttpStatus.UNAUTHORIZED);

        return "error";
    }
}
