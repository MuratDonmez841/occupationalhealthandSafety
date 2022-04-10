package com.cakestudios.occupationalhealthandSafety;

public class Iskollari {
    private String genelProfil;
    private String imgDownloadURL;
    private String isAdi;
    private String malzemeler;
    private String onlemler;
    private String riskler;
    private String key;

    public Iskollari() {
    }

    public Iskollari(String genelProfil, String imgDownloadURL, String isAdi, String malzemeler, String onlemler, String riskler, String key) {
        this.genelProfil = genelProfil;
        this.imgDownloadURL = imgDownloadURL;
        this.isAdi = isAdi;
        this.malzemeler = malzemeler;
        this.onlemler = onlemler;
        this.riskler = riskler;
        this.key = key;
    }

    public String getGenelProfil() {
        return genelProfil;
    }

    public void setGenelProfil(String genelProfil) {
        this.genelProfil = genelProfil;
    }

    public String getImgDownloadURL() {
        return imgDownloadURL;
    }

    public void setImgDownloadURL(String imgDownloadURL) {
        this.imgDownloadURL = imgDownloadURL;
    }

    public String getIsAdi() {
        return isAdi;
    }

    public void setIsAdi(String isAdi) {
        this.isAdi = isAdi;
    }

    public String getMalzemeler() {
        return malzemeler;
    }

    public void setMalzemeler(String malzemeler) {
        this.malzemeler = malzemeler;
    }

    public String getOnlemler() {
        return onlemler;
    }

    public void setOnlemler(String onlemler) {
        this.onlemler = onlemler;
    }

    public String getRiskler() {
        return riskler;
    }

    public void setRiskler(String riskler) {
        this.riskler = riskler;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
