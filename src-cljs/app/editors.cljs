(ns app.editors
  (:require [goog.array]
            [goog.dom]))

(defn- create-editor [el]
  (let [editor (.edit js/ace el)]
    (.setTheme editor "ace/theme/tomorrow")
    (.setOptions editor #js {:maxLines 30})
    (.setReadOnly editor true)
    (.setMode (.getSession editor) "ace/mode/javascript")))

(defn init-editors []
  (goog.array/map (goog.dom/getElementsByClass "code-sample") create-editor))
