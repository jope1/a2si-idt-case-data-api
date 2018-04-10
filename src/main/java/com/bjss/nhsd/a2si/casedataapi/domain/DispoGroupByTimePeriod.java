package com.bjss.nhsd.a2si.casedataapi.domain;

public class DispoGroupByTimePeriod {

    private String dispoGroup;
    private String from;
    private String to;
    private Integer total;

    public DispoGroupByTimePeriod() {
    }

    public DispoGroupByTimePeriod(String dispoGroup, String from, String to, Integer total) {
        this.dispoGroup = dispoGroup;
        this.from = from;
        this.to = to;
        this.total = total;
    }

    public String getDispoGroup() {
        return dispoGroup;
    }

    public void setDispoGroup(String dispoGroup) {
        this.dispoGroup = dispoGroup;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "DispoGroupByTimePeriod{" +
                "dispoGroup='" + dispoGroup + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", total=" + total +
                '}';
    }
}
