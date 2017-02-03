(ns life-clojure.core
  (:require [life-clojure.configuration :as config]
            [clojure.core.matrix :as matrix]
            [clojure.string :as string])
  (:gen-class))

; Options: "blinker", "glider", "toad", "pulsar", "pentadecathlon", "lightweight-spaceship", "gosper-glider-gun"
(def configuration-name "blinker")


(def max-rows (get-in config/configuration [configuration-name :max-rows]))
(def max-columns (get-in config/configuration [configuration-name :max-columns]))


(defn- get-living-neighbors
  "Calculate number of living neighbors of cell in current board."
  [row column cell-value board]
  (let [row-start (max (- row 1) 0)
        row-length (if (or (= row 0) (= row (- max-rows 1))) 2 3)  ; subtract 1 from max-rows because it's 1-indexed
        column-start (max (- column 1) 0)
        column-length (if (or (= column 0) (= column (- max-columns 1))) 2 3)]  ; subtract 1 from max-columns because it's 1-indexed
    (- (matrix/esum (matrix/submatrix board row-start row-length column-start column-length)) cell-value)))


(defn- evolve
  "Return correct value of cell based on neighbors"
  [[row column] cell-value board]
  (let [live-neighbors (get-living-neighbors row column cell-value board)]
    (if (= cell-value 1)  ; cell is currently living
      (cond
        (< live-neighbors 2) 0  ; rule 1
        (> live-neighbors 3) 0  ; rule 3
        (or (= live-neighbors 2) (= live-neighbors 3)) 1)  ; rule 2
      (if (= live-neighbors 3) 1 0))))  ; rule 4


(defn- print-board
  "Print board"
  [board]
  (doseq [row board]
    (println (string/join " " (map #(if (= % 0) "." "O") row))))
  (println))  ; add empty line between boards


(defn- get-initial-board
  "Build and return the initial board"
  [live-cells]
  (let [board (matrix/matrix (make-array Integer/TYPE max-rows max-columns))]  ; build board filled with 0s;
    (reduce (fn [new-board [row column]] (assoc-in new-board [row column] 1)) board live-cells)))  ; set 1 at all locations in 'live-cells'


(defn -main
  "Run the game."
  []
  (let [initial-board (get-initial-board (get-in config/configuration [configuration-name :live-cells]))]
    (println "Starting Conway's Game of Life...\n")
    (print-board initial-board)
    (Thread/sleep 1000)
    (loop [board initial-board]
      (let [updated-board (matrix/emap-indexed #(evolve %1 %2 board) board)]
        (print-board updated-board)
        (Thread/sleep 1000)
        (recur updated-board)))))
