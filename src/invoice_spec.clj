(ns invoice-spec
  (:require
    [clojure.spec.alpha :as s]
    [clojure.data.json :as json]))

(defn load-json
  [path]
  (with-open [reader (clojure.java.io/reader path)]
  (json/read reader)))

(def json-data (load-json "../invoice.json"))

(defn transform-key [k path]
  (let [full-path (clojure.string/join "/" (conj path k))]
    (case full-path
      "invoice" :invoice
      "customer" :customer
      "invoice/issue_date" :invoice/issue-date
      "invoice/order_reference" :invoice/order-reference
      "invoice/items" :invoice/items
      "invoice/items/price" :invoice-item/price
      "invoice/items/quantity" :invoice-item/quantity
      "invoice/items/sku" :invoice-item/sku
      "invoice/items/taxes" :invoice-item/taxes
      "invoice/items/taxes/tax_category" :tax/category
      "invoice/items/taxes/tax_rate" :tax/rate
      "invoice/customer" :invoice/customer
      "invoice/customer/company_name" :customer/name
      "invoice/customer/email" :customer/email
      (keyword k))))

(defn nested-map->keywords
  ([data] (nested-map->keywords data []))
  ([data path]
   (cond
     (map? data) (into {} (for [[k v] data]
                            (let [new-key (transform-key k path)
                                  new-path (conj path (name new-key))]
                              [new-key (nested-map->keywords v new-path)])))
     (vector? data) (vec (map #(nested-map->keywords % path) data))
     :else data)))

(def invoice (nested-map->keywords json-data))
(println invoice)

(defn not-blank? [value] (-> value clojure.string/blank? not))
(defn non-empty-string? [x] (and (string? x) (not-blank? x)))

(s/def :customer/name non-empty-string?)
(s/def :customer/email non-empty-string?)
(s/def :invoice/customer (s/keys :req [:customer/name
                                       :customer/email]))

(s/def :tax/rate double?)
(s/def :tax/category #{:IVA})
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

(s/valid? ::invoice invoice)
