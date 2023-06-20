package io.appmetrica.analytics.impl.component.processor;

public abstract class ProcessingStrategyFactory<BaseHandler> {

    public abstract EventProcessingStrategy<BaseHandler> getProcessingStrategy(int eventType);
}
