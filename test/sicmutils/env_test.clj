;
; Copyright (C) 2016 Colin Smith.
; This work is based on the Scmutils system of MIT/GNU Scheme.
;
; This is free software;  you can redistribute it and/or modify
; it under the terms of the GNU General Public License as published by
; the Free Software Foundation; either version 3 of the License, or (at
; your option) any later version.
;
; This software is distributed in the hope that it will be useful, but
; WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
; General Public License for more details.
;
; You should have received a copy of the GNU General Public License
; along with this code; if not, see <http://www.gnu.org/licenses/>.
;

(ns sicmutils.env-test
  (:refer-clojure :exclude [+ - * / zero? partial ref])
  (:require [clojure.test :refer :all :exclude [function?]]
            [sicmutils.env :refer :all])
  (:import (org.apache.commons.math3.complex Complex)))

(deftest partial-shim
  (testing "partial also works the way Clojure defines it"
    (is (= 10 ((partial + 9) 1)))
    (is (= 5 ((partial max 4) 5)))
    (is (= 4 ((partial max 4) 2)))))

(deftest ref-shim
  (testing "works for structures"
    (is (= 2 (ref (up 1 2 3) 1)))
    (is (= 3 (ref (down (up 1 2) (up 3 4)) 1 0))))
  (testing "works clojure-style"
    (let [r (ref [])
          s (ref {} :meta {:a "apple"})]
      (is (= [] @r))
      (is (= [99] (dosync (alter r conj 99))))
      (is (= {:b 88} (dosync (alter s assoc :b 88)))))))

(deftest literal-function-shim
  (testing "works for signatures"
    (let [f1 (literal-function 'f)
          f2 (literal-function 'f (-> Real Real))
          f3 (literal-function 'f (-> Real (UP Real Real)))
          f4 (literal-function 'f [0] (up 1 2))
          f5 (literal-function 'f (-> (DOWN Real Real) (X Real Real)))]
      (is (= '(f x) (simplify (f1 'x))))
      (is (= '(f x) (simplify (f2 'x))))
      (is (= '(up (f↑0 x) (f↑1 x)) (simplify (f3 'x))))
      (is (= '(up (f↑0 x) (f↑1 x)) (simplify (f4 'x))))
      (is (= '(up (f↑0 (down p_x p_y)) (f↑1 (down p_x p_y)))
             (simplify (f5 (down 'p_x 'p_y))))))))

(deftest shortcuts
  (testing "cot"
    (is (= '(/ (cos x) (sin x)) (simplify (cot 'x))))
    (is (= '(/ 1 (sin x)) (simplify (csc 'x))))
    (is (= '(/ 1 (cos x)) (simplify (sec 'x))))
    (is (= (Complex. 1 2) (complex 1 2)))
    (is (= :sicmutils.structure/up (orientation (up 1 2))))
    (is (= "up(b z - c y, - a z + c x, a y - b x)"
           (->infix (simplify (cross-product (up 'a 'b 'c) (up 'x 'y 'z))))))))

(deftest matrices
  (testing "qp-submatrix"
    (let [A (matrix-by-rows [1 2 3]
                            [4 5 6]
                            [7 8 9])]
      (is (= (matrix-by-rows [5 6]
                             [8 9])
             (qp-submatrix A))))))
