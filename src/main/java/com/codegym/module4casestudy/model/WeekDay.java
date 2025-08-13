package com.codegym.module4casestudy.model;

public enum WeekDay {
    MON, TUE, WED, THU, FRI, SAT, SUN;

    public String vnLabel() {
        switch (this) {
            case MON: return "Thứ 2";
            case TUE: return "Thứ 3";
            case WED: return "Thứ 4";
            case THU: return "Thứ 5";
            case FRI: return "Thứ 6";
            case SAT: return "Thứ 7";
            case SUN: return "Chủ nhật";
            default:  return "";
        }
    }

    public String shortLabel() {
        switch (this) {
            case MON: return "T2";
            case TUE: return "T3";
            case WED: return "T4";
            case THU: return "T5";
            case FRI: return "T6";
            case SAT: return "T7";
            case SUN: return "CN";
            default:  return "";
        }
    }
}
