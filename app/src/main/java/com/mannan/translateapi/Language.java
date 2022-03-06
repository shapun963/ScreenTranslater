package com.mannan.translateapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Language {
    public final String name;
    public final String code;
	public static final String AUTO_DETECT = "auto";
    public static final List<Language> LANGUAGES = new ArrayList<>();
	
    public Language(String name, String code) {
        this.name = name;
        this.code = code;
    }
	@Override
	public boolean equals(Object obj){
		if (this == obj)return true;
        if (obj == null)return false;
        if (getClass() != obj.getClass())return false;
        final Language  other = (Language) obj;
        if (!name.equals(other.name))return false;
		if (!code.equals(other.code))return false;
        return true;
	}
	@Override
    public int hashCode() {
        return Objects.hash(name,code);
    }
	
	public static boolean isSupported(String code){
		for(Language language:LANGUAGES){
			if(language.code.equals(code)){
				return true;
			}
		}
		return false;
	}
	public static Language getLanguage(String code){
		for(Language language:LANGUAGES){
			if(language.code.equals(code)){
				return language;
			}
		}
		return null;
	}
	
    // Supported languages list from https://cloud.google.com/translate/docs/languages
    static {
        LANGUAGES.add(new Language("Afrikaans", "af"));
        LANGUAGES.add(new Language("Albanian", "sq"));
        LANGUAGES.add(new Language("Amharic", "am"));
        LANGUAGES.add(new Language("Arabic", "ar"));
        LANGUAGES.add(new Language("Armenian", "hy"));
        LANGUAGES.add(new Language("Azerbaijani", "az"));
        LANGUAGES.add(new Language("Basque", "eu"));
        LANGUAGES.add(new Language("Belarusian", "be"));
        LANGUAGES.add(new Language("Bengali", "bn"));
        LANGUAGES.add(new Language("Bosnian", "bs"));
        LANGUAGES.add(new Language("Bulgarian", "bg"));
        LANGUAGES.add(new Language("Catalan", "ca"));
        LANGUAGES.add(new Language("Cebuano", "ceb (ISO-639-2)"));
        LANGUAGES.add(new Language("Chinese (Simplified)", "zh-CN or zh (BCP-47)"));
        LANGUAGES.add(new Language("Chinese (Traditional)", "zh-TW (BCP-47)"));
        LANGUAGES.add(new Language("Corsican", "co"));
        LANGUAGES.add(new Language("Croatian", "hr"));
        LANGUAGES.add(new Language("Czech", "cs"));
        LANGUAGES.add(new Language("Danish", "da"));
        LANGUAGES.add(new Language("Dutch", "nl"));
        LANGUAGES.add(new Language("English", "en"));
        LANGUAGES.add(new Language("Esperanto", "eo"));
        LANGUAGES.add(new Language("Estonian", "et"));
        LANGUAGES.add(new Language("Finnish", "fi"));
        LANGUAGES.add(new Language("French", "fr"));
        LANGUAGES.add(new Language("Frisian", "fy"));
        LANGUAGES.add(new Language("Galician", "gl"));
        LANGUAGES.add(new Language("Georgian", "ka"));
        LANGUAGES.add(new Language("German", "de"));
        LANGUAGES.add(new Language("Greek", "el"));
        LANGUAGES.add(new Language("Gujarati", "gu"));
        LANGUAGES.add(new Language("Haitian Creole", "ht"));
        LANGUAGES.add(new Language("Hausa", "ha"));
        LANGUAGES.add(new Language("Hawaiian", "haw (ISO-639-2)"));
        LANGUAGES.add(new Language("Hebrew", "he or iw"));
        LANGUAGES.add(new Language("Hindi", "hi"));
        LANGUAGES.add(new Language("Hmong", "hmn (ISO-639-2)"));
        LANGUAGES.add(new Language("Hungarian", "hu"));
        LANGUAGES.add(new Language("Icelandic", "is"));
        LANGUAGES.add(new Language("Igbo", "ig"));
        LANGUAGES.add(new Language("Indonesian", "id"));
        LANGUAGES.add(new Language("Irish", "ga"));
        LANGUAGES.add(new Language("Italian", "it"));
        LANGUAGES.add(new Language("Japanese", "ja"));
        LANGUAGES.add(new Language("Javanese", "jv"));
        LANGUAGES.add(new Language("Kannada", "kn"));
        LANGUAGES.add(new Language("Kazakh", "kk"));
        LANGUAGES.add(new Language("Khmer", "km"));
        LANGUAGES.add(new Language("Kinyarwanda", "rw"));
        LANGUAGES.add(new Language("Korean", "ko"));
        LANGUAGES.add(new Language("Kurdish", "ku"));
        LANGUAGES.add(new Language("Kyrgyz", "ky"));
        LANGUAGES.add(new Language("Lao", "lo"));
        LANGUAGES.add(new Language("Latvian", "lv"));
        LANGUAGES.add(new Language("Lithuanian", "lt"));
        LANGUAGES.add(new Language("Luxembourgish", "lb"));
        LANGUAGES.add(new Language("Macedonian", "mk"));
        LANGUAGES.add(new Language("Malagasy", "mg"));
        LANGUAGES.add(new Language("Malay", "ms"));
        LANGUAGES.add(new Language("Malayalam", "ml"));
        LANGUAGES.add(new Language("Maltese", "mt"));
        LANGUAGES.add(new Language("Maori", "mi"));
        LANGUAGES.add(new Language("Marathi", "mr"));
        LANGUAGES.add(new Language("Mongolian", "mn"));
        LANGUAGES.add(new Language("Myanmar (Burmese)", "my"));
        LANGUAGES.add(new Language("Nepali", "ne"));
        LANGUAGES.add(new Language("Norwegian", "no"));
        LANGUAGES.add(new Language("Nyanja (Chichewa)", "ny"));
        LANGUAGES.add(new Language("Odia (Oriya)", "or"));
        LANGUAGES.add(new Language("Pashto", "ps"));
        LANGUAGES.add(new Language("Persian", "fa"));
        LANGUAGES.add(new Language("Polish", "pl"));
        LANGUAGES.add(new Language("Portuguese (Portugal, Brazil)", "pt"));
        LANGUAGES.add(new Language("Punjabi", "pa"));
        LANGUAGES.add(new Language("Romanian", "ro"));
        LANGUAGES.add(new Language("Russian", "ru"));
        LANGUAGES.add(new Language("Samoan", "sm"));
        LANGUAGES.add(new Language("Scots Gaelic", "gd"));
        LANGUAGES.add(new Language("Serbian", "sr"));
        LANGUAGES.add(new Language("Sesotho", "st"));
        LANGUAGES.add(new Language("Shona", "sn"));
        LANGUAGES.add(new Language("Sindhi", "sd"));
        LANGUAGES.add(new Language("Sinhala (Sinhalese)", "si"));
        LANGUAGES.add(new Language("Slovak", "sk"));
        LANGUAGES.add(new Language("Slovenian", "sl"));
        LANGUAGES.add(new Language("Somali", "so"));
        LANGUAGES.add(new Language("Spanish", "es"));
        LANGUAGES.add(new Language("Sundanese", "su"));
        LANGUAGES.add(new Language("Swahili", "sw"));
        LANGUAGES.add(new Language("Swedish", "sv"));
        LANGUAGES.add(new Language("Tagalog (Filipino)", "tl"));
        LANGUAGES.add(new Language("Tajik", "tg"));
        LANGUAGES.add(new Language("Tamil", "ta"));
        LANGUAGES.add(new Language("Tatar", "tt"));
        LANGUAGES.add(new Language("Telugu", "te"));
        LANGUAGES.add(new Language("Thai", "th"));
        LANGUAGES.add(new Language("Turkish", "tr"));
        LANGUAGES.add(new Language("Turkmen", "tk"));
        LANGUAGES.add(new Language("Ukrainian", "uk"));
        LANGUAGES.add(new Language("Urdu", "ur"));
        LANGUAGES.add(new Language("Uyghur", "ug"));
        LANGUAGES.add(new Language("Uzbek", "uz"));
        LANGUAGES.add(new Language("Vietnamese", "vi"));
        LANGUAGES.add(new Language("Welsh", "cy"));
        LANGUAGES.add(new Language("Xhosa", "xh"));
        LANGUAGES.add(new Language("Yiddish", "yi"));
        LANGUAGES.add(new Language("Yoruba", "yo"));
        LANGUAGES.add(new Language("Zulu", "zu"));
    }
}
