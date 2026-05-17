package com.hasiruusiru.app.model

data class Species(
    val nameEnglish: String,
    val nameScientific: String,
    val nameKannada: String,
    val benefitEnglish: String,
    val benefitKannada: String,
    val imageResId: Int = 0 // Placeholder for image resource
)

object SpeciesData {
    val nativeTrees = listOf(
        Species(
            nameEnglish = "Neem",
            nameScientific = "Azadirachta indica",
            nameKannada = "ಬೇವು (Bevu)",
            benefitEnglish = "Air purification & Pest control",
            benefitKannada = "ಗಾಳಿ ಶುದ್ಧೀಕರಣ ಮತ್ತು ಕೀಟ ನಿಯಂತ್ರಣ"
        ),
        Species(
            nameEnglish = "Honge",
            nameScientific = "Millettia pinnata",
            nameKannada = "ಹೊಂಗೆ (Honge)",
            benefitEnglish = "Bio-fuel & heavy shade",
            benefitKannada = "ಜೈವಿಕ ಇಂಧನ ಮತ್ತು ದಟ್ಟವಾದ ನೆರಳು"
        ),
        Species(
            nameEnglish = "Peepal",
            nameScientific = "Ficus religiosa",
            nameKannada = "ಅಶ್ವತ್ಥ (Ashwatha)",
            benefitEnglish = "Maximum O2 production",
            benefitKannada = "ಗರಿಷ್ಠ ಆಮ್ಲಜನಕ ಉತ್ಪಾದನೆ"
        ),
        Species(
            nameEnglish = "Banyan",
            nameScientific = "Ficus benghalensis",
            nameKannada = "ಆಲದ ಮರ (Alada Mara)",
            benefitEnglish = "Soil conservation & bird habitat",
            benefitKannada = "ಮಣ್ಣಿನ ಸಂರಕ್ಷಣೆ ಮತ್ತು ಪಕ್ಷಿಗಳ ಆವಾಸಸ್ಥಾನ"
        )
    )
}
