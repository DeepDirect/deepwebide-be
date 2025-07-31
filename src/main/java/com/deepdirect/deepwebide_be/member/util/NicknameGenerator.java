package com.deepdirect.deepwebide_be.member.util;

import java.util.List;
import java.util.Random;

public class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "겁쟁이인", "고상한", "고통스러운", "과묵한", "광기어린", "괴상한", "권태로운", "귀여운", "귀찮은", "기쁜",
            "까칠한", "깨끗한", "끈질긴", "날카로운", "노련한", "느긋한", "느린", "달콤한", "따뜻한", "똑똑한",
            "뜨거운", "멍한", "무뚝뚝한", "무서운", "반짝이는", "밝은", "배고픈", "부끄러운", "분노한", "불안한",
            "빠른", "산만한", "상냥한", "상쾌한", "서투른", "수줍은", "쉬운", "슬기로운", "시끄러운", "신난",
            "심란한", "심심한", "쓸쓸한", "애매한", "어두운", "어려운", "엉뚱한", "엉망진창인", "엉큼한", "여유로운",
            "예민한", "요란한", "요망한", "용감한", "우는", "우아한", "우울한", "웃는", "의젓한", "자신감넘치는",
            "자유로운", "작은", "장난스런", "재빠른", "정신없는", "정중한", "조급한", "조용한", "졸린", "즐거운",
            "진지한", "차가운", "침착한", "큰", "평범한", "피곤한", "행복한", "호기심 많은", "화난", "활기찬",
            "활발한", "흥분한", "희망찬"
    );

    private static final Random RANDOM = new Random();

    public static String generate() {
        String adj = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
        return adj + " 개발자";
    }
}
