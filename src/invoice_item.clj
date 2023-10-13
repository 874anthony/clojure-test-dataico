(ns invoice-item
  (:require [clojure.test :refer :all]))

(defn- discount-factor [{:invoice-item/keys [discount-rate]
                         :or                {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))

(defn subtotal
  [{:invoice-item/keys [precise-quantity precise-price discount-rate]
    :as                item
    :or                {discount-rate 0}}]
  (* precise-price precise-quantity (discount-factor item)))

(deftest test-subtotal
  (testing "Basic case, no discount"
    (is (= 100.0 (subtotal {:invoice-item/precise-quantity 10
                          :invoice-item/precise-price 10}))))

  (testing "100% discount rate"
    (is (= 0.0 (subtotal {:invoice-item/precise-quantity 10
                      :invoice-item/precise-price 10
                      :invoice-item/discount-rate 100}))))

  (testing "Basic case, with 10% discount"
    (is (= 90.0 (subtotal {:invoice-item/precise-quantity 10
                         :invoice-item/precise-price 10
                         :invoice-item/discount-rate 10}))))

  (testing "Fractional discount rates"
    (is (= 99.5 (subtotal {:invoice-item/precise-quantity 10
                           :invoice-item/precise-price 10
                           :invoice-item/discount-rate 0.5}))))

  (testing "Zero price"
    (is (= 0.0 (subtotal {:invoice-item/precise-quantity 10
                        :invoice-item/precise-price 0}))))
  )