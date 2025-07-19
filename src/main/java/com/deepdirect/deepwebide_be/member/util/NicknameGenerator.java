package com.deepdirect.deepwebide_be.member.util;

import java.util.List;
import java.util.Random;

public class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "슬기로운", "권태로운", "우는", "웃는", "기쁜", "화난", "차가운", "뜨거운", "평범한", "용감한", "행복한",
            "느긋한", "조용한", "시끄러운", "상냥한", "무뚝뚝한", "겁쟁이인", "귀여운", "엉뚱한", "멍한", "똑똑한", "배고픈",
            "침착한", "우울한", "광기어린", "수줍은", "분노한", "우아한", "애매한", "날카로운", "귀찮은", "엉망진창인",
            "반짝이는", "무서운", "요란한", "재빠른", "정중한", "자신감 넘치는", "불안한", "느린", "심심한", "정신없는",
            "끈질긴", "달콤한", "과묵한", "산만한", "진지한", "까칠한", "활기찬", "졸린", "자유로운", "호기심 많은", "고통스러운"
    );

    private static final Random RANDOM = new Random();

    public static String generate() {
        String adj = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
        return adj + " 개발자";
    }
}
