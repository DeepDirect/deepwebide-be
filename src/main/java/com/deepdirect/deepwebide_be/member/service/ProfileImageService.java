package com.deepdirect.deepwebide_be.member.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class ProfileImageService {

    public String generateProfileImageUrl(String nickname, int size) {
        String initials = getInitials(nickname);
        String bgColor = getBackgroundColor(nickname);
        String svg = buildSvg(initials, bgColor, size);
        return encodeSvgToBase64(svg);
    }

    private String getInitials(String nickname) {
        if (nickname == null || nickname.isBlank()) return "NN";

        String[] words = nickname.trim().split("\\s+");

        StringBuilder initials = new StringBuilder();

        for (String word : words) {
            if (!word.isBlank()) {
                initials.append(word.charAt(0)); // 각 단어 첫 글자
            }

            if (initials.length() >= 2) break; // 최대 2글자까지만
        }

        return !initials.isEmpty() ? initials.toString() : "NN";
    }

    private static final String[] SAFE_BG_COLORS = {
            "#FFE599", "#B7E1CD", "#AED6F1", "#FFDAB9", "#F5CBA7", "#D7BDE2", "#D5F5E3"
    };

    private String getBackgroundColor(String seed) {
        int index = Math.abs(seed.hashCode()) % SAFE_BG_COLORS.length;
        return SAFE_BG_COLORS[index];
    }

    private String buildSvg(String initials, String bgColor, int size) {
        int fontSize = 15;
        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d">
              <rect width="100%%" height="100%%" fill="%s"/>
              <text x="50%%" y="55%%" font-size="%d" fill="black" font-weight="bold" text-anchor="middle" dominant-baseline="middle" font-family="Inter">%s</text>
            </svg>
            """, size, size, bgColor, fontSize, initials);
    }

    private String encodeSvgToBase64(String svg) {
        String base64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64;
    }
}
