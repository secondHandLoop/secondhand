package com.secondhand.promotion.utils;



import com.secondhand.common.doc.DocUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocBuilder {
    public static void main(String[] args) {
        DocUtils.generate("com.secondhand.promotion.api");
        log.info("doc build for promotion success...........................");
    }
}

