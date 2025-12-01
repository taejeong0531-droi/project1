package com.example.myapplication

object FoodImages {
    private val map: Map<String, String> = mapOf(
        // Korean foods
        "짬뽕" to "https://images.unsplash.com/photo-1604908554007-027c9e5f2c0b?q=80&w=800&auto=format&fit=crop",
        "삼겹살" to "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=800&auto=format&fit=crop",
        "떡볶이" to "https://images.unsplash.com/photo-1611905301670-1a3c2aaca5f2?q=80&w=800&auto=format&fit=crop",
        "마라탕" to "https://images.unsplash.com/photo-1612927601624-b1e9e19794b0?q=80&w=800&auto=format&fit=crop",
        "불고기" to "https://images.unsplash.com/photo-1604908553927-3e2d1b845a70?q=80&w=800&auto=format&fit=crop",
        "카레" to "https://images.unsplash.com/photo-1604908553831-0f88e21a3654?q=80&w=800&auto=format&fit=crop",
        "미역국" to "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?q=80&w=800&auto=format&fit=crop",
        "닭죽" to "https://images.unsplash.com/photo-1514511547110-9fdc3b3c0c54?q=80&w=800&auto=format&fit=crop",
        "칼국수" to "https://images.unsplash.com/photo-1606756790138-261d2bdf87cd?q=80&w=800&auto=format&fit=crop",
        "된장찌개" to "https://images.unsplash.com/photo-1617093727343-3702d061f1c1?q=80&w=800&auto=format&fit=crop",
        "솥밥" to "https://images.unsplash.com/photo-1505577058444-a3dab90d4253?q=80&w=800&auto=format&fit=crop",
        "닭가슴살 샐러드" to "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?q=80&w=800&auto=format&fit=crop",
        "연어덮밥" to "https://images.unsplash.com/photo-1553621042-f6e147245754?q=80&w=800&auto=format&fit=crop",
        "두부구이" to "https://images.unsplash.com/photo-1596040033229-c5c4b8b96d02?q=80&w=800&auto=format&fit=crop",
        "콩나물국밥" to "https://images.unsplash.com/photo-1604908554007-027c9e5f2c0b?q=80&w=800&auto=format&fit=crop",
        "비빔밥" to "https://images.unsplash.com/photo-1617692855027-bf8c2c1b0a46?q=80&w=800&auto=format&fit=crop",
        "치킨" to "https://images.unsplash.com/photo-1604908553776-7f3e2d9fe8d3?q=80&w=800&auto=format&fit=crop",
        "파스타" to "https://images.unsplash.com/photo-1526312426976-593c2b999512?q=80&w=800&auto=format&fit=crop",
        "피자" to "https://images.unsplash.com/photo-1548365328-8b6dbfbf7f9e?q=80&w=800&auto=format&fit=crop",
        "햄버거" to "https://images.unsplash.com/photo-1550547660-d9450f859349?q=80&w=800&auto=format&fit=crop",
        "크림리조또" to "https://images.unsplash.com/photo-1612874744027-415a2ddc7b17?q=80&w=800&auto=format&fit=crop",
        "김치찌개" to "https://images.unsplash.com/photo-1585238342028-4bbc1a17f5d3?q=80&w=800&auto=format&fit=crop",
        "순두부찌개" to "https://images.unsplash.com/photo-1585238342028-4bbc1a17f5d3?q=80&w=800&auto=format&fit=crop",
        "비빔국수" to "https://images.unsplash.com/photo-1526312426976-593c2b999512?q=80&w=800&auto=format&fit=crop",
        "샌드위치" to "https://images.unsplash.com/photo-1550317138-10000687a72b?q=80&w=800&auto=format&fit=crop",
        // local fallback items
        "상큼 과일 샐러드" to "https://images.unsplash.com/photo-1540420773420-3366772f4999?q=80&w=800&auto=format&fit=crop",
        "베리 요거트" to "https://images.unsplash.com/photo-1514996937319-344454492b37?q=80&w=800&auto=format&fit=crop",
        "망고 스무디" to "https://images.unsplash.com/photo-1621263764928-3d2e115c9b14?q=80&w=800&auto=format&fit=crop",
        "요거트 파르페" to "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=800&auto=format&fit=crop",
        "딸기 케이크" to "https://images.unsplash.com/photo-1495147466023-ac5c588e2e94?q=80&w=800&auto=format&fit=crop",
        "바나나 팬케이크" to "https://images.unsplash.com/photo-1559628233-9eee7e3b5f2b?q=80&w=800&auto=format&fit=crop",
        "매콤 치킨" to "https://images.unsplash.com/photo-1604908177079-7251f1ed40d2?q=80&w=800&auto=format&fit=crop",
        "핫 칠리 라면" to "https://images.unsplash.com/photo-1605478069193-1cba4cc58c41?q=80&w=800&auto=format&fit=crop",
        "청양고추 피자" to "https://images.unsplash.com/photo-1548365328-8b6dbfbf7f9e?q=80&w=800&auto=format&fit=crop",
        "마라샹궈" to "https://images.unsplash.com/photo-1612874744027-415a2ddc7b17?q=80&w=800&auto=format&fit=crop",
        "연어 샐러드" to "https://images.unsplash.com/photo-1553621042-f6e147245754?q=80&w=800&auto=format&fit=crop",
        "녹차" to "https://images.unsplash.com/photo-1470167290877-7d5d3446de4c?q=80&w=800&auto=format&fit=crop",
        "현미밥 정식" to "https://images.unsplash.com/photo-1625944523320-24c3a0f0e7df?q=80&w=800&auto=format&fit=crop",
        "야채 수프" to "https://images.unsplash.com/photo-1547592180-85f173990554?q=80&w=800&auto=format&fit=crop"
    )

    fun urlFor(name: String?): String? {
        if (name.isNullOrBlank()) return null
        return map[name]
    }
}
