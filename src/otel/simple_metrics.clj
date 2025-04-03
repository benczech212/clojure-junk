
(ns otel.simple_metrics
  (:import [io.opentelemetry.api GlobalOpenTelemetry]
           [io.opentelemetry.api.metrics Meter]
           [io.opentelemetry.api.common Attributes]
           [io.opentelemetry.sdk OpenTelemetrySdk]
           [io.opentelemetry.sdk.metrics SdkMeterProvider]
           [io.opentelemetry.sdk.metrics.export PeriodicMetricReader]
           [io.opentelemetry.exporter.otlp.metrics OtlpGrpcMetricExporter]
           [java.time Duration]))

(defn setup-otel []
  ;; Create exporter
  (let [exporter (-> (OtlpGrpcMetricExporter/builder)
                     (.setEndpoint "http://localhost:4317") ;; Alloy OTLP gRPC endpoint
                     (.build))

        ;; Metric reader with 10s export interval
        metric-reader (-> (PeriodicMetricReader/builder exporter)
                          (.setInterval (Duration/ofSeconds 10))
                          (.build))

        ;; Meter provider
        meter-provider (-> (SdkMeterProvider/builder)
                           (.registerMetricReader metric-reader)
                           (.build))]

    ;; Register OTEL globally
    (-> (OpenTelemetrySdk/builder)
        (.setMeterProvider meter-provider)
        (.buildAndRegisterGlobal))

    ;; Return Meter
    (-> (GlobalOpenTelemetry/get)
        (.getMeterProvider)
        (.get "basic-clojure-metrics"))))

(defn -main []
  (let [meter (setup-otel)
        counter (-> meter
                    (.counterBuilder "clj_example_counter")
                    (.setDescription "Example counter from Clojure to Alloy")
                    (.setUnit "1")
                    (.build))]

    ;; Basic infinite loop adding to counter
    (loop []
      (.add counter 1)
      (println "incremented clj_example_counter")
      (Thread/sleep 5000)
      (recur))))
