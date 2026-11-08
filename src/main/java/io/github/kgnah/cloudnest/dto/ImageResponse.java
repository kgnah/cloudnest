package io.github.kgnah.cloudnest.dto;


public class ImageResponse {

    private String id;
    private String repository;
    private String tag;
    private Long size;


    public ImageResponse(String id, String repository, String tag, Long size) {
        this.id = id;
        this.repository = repository;
        this.tag = tag;
        this.size = size;
    }

    public String getId() {return id;}
    public String getRepository() {return repository;}
    public String getTag() {return tag;}
    public Long getSize() {return size;}
}
