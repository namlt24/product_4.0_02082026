package com.bccs.gatewaymanager.dto;

/** 1 endpoint trong so do phu thuoc - kem so lieu de hien thi badge tren UI. */
public record GraphNodeDto(
        String id,
        String name,
        String path,
        String method,
        boolean sequential,
        int stepCount,
        /** So endpoint KHAC dang goi ngươc vao endpoint nay (qua host tro ve chinh KrakenD). */
        int usedByCount,
        /** So endpoint KHAC ma chinh endpoint nay goi toi. */
        int callsCount,
        /** true neu endpoint nay nam trong 1 vong lap phu thuoc (A goi B, B goi lai A...). */
        boolean inCycle,
        /** Do sau trong so do (0 = khong goi endpoint nao khac / la, cang cao cang gan "entry point"). Dung de layout. */
        int layer,
        /**
         * ID cua "cum lien thong" (connected component, tinh ca 2 chieu goi/duoc-goi) - cac
         * endpoint co cung componentId thi co lien quan (truc tiep hoac gian tiep) voi nhau,
         * nen ve chung trong 1 cum tren so do. Dung de FE khong phai tu tinh lai thuat toan nay
         * (quan trong khi so luong endpoint len toi hang ngan - tinh 1 lan o backend, FE chi render).
         */
        int componentId,
        /** true neu endpoint nay khong co quan he phu thuoc nao (khong goi ai, khong bi ai goi) - FE nen an mac dinh khoi so do khi so luong lon. */
        boolean isolated
) {
}
