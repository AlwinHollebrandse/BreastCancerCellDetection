package com.alwin;

public class LabelExtraction {

//    actual labels
//  50 columnar epithelial cells
//• 50 parabasal squamous epithelial cells
//• 50 intermediate squamous epithelial cells
//• 50 superficial squamous epithelial cells
//• 100 mild nonkeratinizing dysplastic cells
//• 100 moderate nonkeratinizing dysplastic cells
//• 100 severe nonkeratinizing dysplastic cells

//    file names given
//    50 cyl
//    50 para
//    50 inter
//    50 super
//    100 mod
//    100 let
//    106 svar





    public String getCellClassLabel(String imageFileName) {
        StringBuilder result = new StringBuilder();
        int forLoopEnd = imageFileName.length()  - 4; // the -4 "removes" the ".BMP"
        for (int i = 0; i < forLoopEnd; i++) {
            if (Character.isLetter(imageFileName.charAt(i))) {
                result.append(imageFileName.charAt(i));
            }
        }
        return result.toString();
//
//
//        String result = "";
//        if (imageFileName.contains("cyl")) {
//            return cyl;
//        }
//        return result;
    }
}
