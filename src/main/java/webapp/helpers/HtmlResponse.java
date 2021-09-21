package webapp.helpers;

import helpers.Constants;
import webappexceptions.TemplateNotFoundException;

import java.io.*;
import java.nio.file.Files;


public class HtmlResponse {
    private static String TEMPLATES_DIR = Constants.APP_TEMPLATES_FOLDER;

    private static String readTemplateFile(String filepath) throws TemplateNotFoundException{
        Class htmlResponseCLass = HtmlResponse.class;
        StringBuilder fileContent = new StringBuilder();
        try{
            File file = new File(filepath);
            return new String(Files.readAllBytes(file.toPath()));
            }catch(Exception e) {
            throw new TemplateNotFoundException("\"" + filepath + "\" does not exist !");
        }
    }

    public static String getTemplate(String path)throws TemplateNotFoundException{
        return HtmlResponse.readTemplateFile(HtmlResponse.TEMPLATES_DIR+path);
    }

    public static void setTemplatesDir(String templatesDir) {
        TEMPLATES_DIR = templatesDir;
    }
}
