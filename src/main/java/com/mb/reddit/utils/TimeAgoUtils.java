package com.mb.reddit.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeAgoUtils {

    public static String getTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) return "";

        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return "just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        long days = hours / 24;
        if (days < 7) return days + " day" + (days > 1 ? "s" : "") + " ago";
        long weeks = days / 7;
        if (weeks < 4) return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        long months = days / 30;
        if (months < 12) return months + " month" + (months > 1 ? "s" : "") + " ago";
        long years = days / 365;
        return years + " year" + (years > 1 ? "s" : "") + " ago";
    }
}