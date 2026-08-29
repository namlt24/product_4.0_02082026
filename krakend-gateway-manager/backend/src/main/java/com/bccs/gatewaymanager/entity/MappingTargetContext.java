package com.bccs.gatewaymanager.entity;

/**
 * 1 FieldMapping dung de xay dung loi goi CHINH cua step dich (MAIN, mac
 * dinh - 100% mapping tu truoc gio), hoac loi goi BU TRU/rollback cua step
 * do khi chuoi that bai (COMPENSATION, xem BackendStep.compensationUpstreamService)
 * - xem CompositeOrchestratorEngine.executeCompensationStep().
 */
public enum MappingTargetContext {
    MAIN,
    COMPENSATION
}
