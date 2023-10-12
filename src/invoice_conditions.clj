(ns invoice-conditions)

(def invoice (clojure.edn/read-string (slurp "../invoice.edn")))

(defn getItemsWithTaxOrRet
  "Get all items with tax and retentions in the invoice"
  [string]
  (let [items (get-in string [:invoice/items])]
    (let [filtered-items (filter (fn [item]
           (or (some? (:taxable/taxes item))
                (some? (:retentionable/retentions item))))
                 items)]
      (vec filtered-items))))

(defn isIva19?
  "Check if the item has IVA 19%"
  [item]
  (some (fn [tax]
          (and (= (:tax/category tax) :iva)
               (= (:tax/rate tax) 19)))
        (:taxable/taxes item)))

(defn isRetFuente1?
  "Check if the item has Retencion Fuente 1%"
  [item]
  (some (fn [ret]
          (and (= (:retention/category ret) :ret_fuente)
               (= (:retention/rate ret) 1)))
        (:retentionable/retentions item)))

(defn meetBothConditions?
  "Check if the item has IVA 19% and Retencion Fuente 1% conditions"
  [item]
  (and (isIva19? item)
       (isRetFuente1? item)))

(defn meetEitherCondition?
  "Check if the item has IVA 19% or Retencion Fuente 1% condition"
  [item]
  (or (isIva19? item)
      (isRetFuente1? item)))

(defn getValidItems
  "Get the items with either IVA 19% or Retencion Fuente 1%"
  [listItems]
  (->> listItems
       (filter (complement meetBothConditions?))
       (filter meetEitherCondition?)))

(def listItems (getItemsWithTaxOrRet invoice)) ; Get all items with tax and retentions in the invoice
(getValidItems listItems)