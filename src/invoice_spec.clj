(ns invoice-spec
  (:require
    [clojure.spec.alpha :as s]
    [clojure.data.json :as json]))

(defn load-json
  [path]
  (with-open [reader (clojure.java.io/reader path)]
  (json/read reader)))

(def json-data (load-json "../invoice.json"))

(def initial-key (keys json-data))
(def get-invoice-data (get-in json-data initial-key))
(def keysInvoice (keys get-invoice-data))

(defn getKeysAsKeywords
  [m]
  (map keyword (keys m)))

(def clojure-map
  (zipmap
    (vec (getKeysAsKeywords get-invoice-data))
    (let [invoice-data (get json-data "invoice")]
      (vec (map #(get invoice-data %) keysInvoice)))))

;(println clojure-map)

(def isInvoiceMap (sequential? (get (get json-data "invoice") "items")))
(println (get (get json-data "invoice") "items"))
(println isInvoiceMap)

(defn not-blank? [value] (-> value clojure.string/blank? not))
(defn non-empty-string? [x] (and (string? x) (not-blank? x)))

(s/def :customer/name non-empty-string?)
(s/def :customer/email non-empty-string?)
(s/def :invoice/customer (s/keys :req [:customer/name
                                       :customer/email]))

(s/def :tax/rate double?)
(s/def :tax/category #{:iva})
(s/def ::tax (s/keys :req [:tax/category
                           :tax/rate]))
(s/def :invoice-item/taxes (s/coll-of ::tax :kind vector? :min-count 1))

(s/def :invoice-item/price double?)
(s/def :invoice-item/quantity double?)
(s/def :invoice-item/sku non-empty-string?)

(s/def ::invoice-item
  (s/keys :req [:invoice-item/price
                :invoice-item/quantity
                :invoice-item/sku
                :invoice-item/taxes]))

(s/def :invoice/issue-date inst?)
(s/def :invoice/items (s/coll-of ::invoice-item :kind vector? :min-count 1))

(s/def ::invoice
  (s/keys :req [:invoice/issue-date
                :invoice/customer
                :invoice/items]))