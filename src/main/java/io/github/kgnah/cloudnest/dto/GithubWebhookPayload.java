package io.github.kgnah.cloudnest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubWebhookPayload {

    private String ref;
    private Repository repository;

    public String getRef() {return ref;}

    public void setRef(String ref) {this.ref = ref;}

    public Repository getRepository() {return repository;}

    public void setRepository(Repository repository) {this.repository = repository;}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {

        private String clone_url;

        public String getClone_url() {return clone_url;}

        public void setClone_url(String clone_url) {this.clone_url = clone_url;}
    }


}
