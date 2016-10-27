#!/bin/bash

output_dir=$1

coinventors_locations_assignees_weights=("9.5" "10.0" "10.5")
lawyers_weights=("4.0" "5.0")
middle_no_name_penalties=("0.25" "0.35")
middle_author_weights=("3.0" "4.0")
classification_weights=("1.0" "1.5")

setting_no=0

grid_settings="grid_settings.txt"
echo "setting_no	coinventors_locations_assignees_weight	lawyers_weight	middle_no_name_penalty	middle_author_weight	classification_weight" > $grid_settings

for coinventors_locations_assignees_weight in "${coinventors_locations_assignees_weights[@]}"; do
    for lawyers_weight in "${lawyers_weights[@]}"; do
        for middle_no_name_penalty in "${middle_no_name_penalties[@]}"; do
            for middle_author_weight in "${middle_author_weights[@]}"; do
               for classification_weight in "${classification_weights[@]}"; do

setting_no=$(( setting_no + 1 ))
echo "$setting_no	$coinventors_locations_assignees_weight	$lawyers_weight	$middle_no_name_penalty	$middle_author_weight	$classification_weight" >> $grid_settings

echo "--model-author-bag-first-initial-weight=20.0
--model-author-bag-first-noname-penalty=4.0
--model-author-bag-first-name-weight=6.0
--model-author-bag-first-saturation=40.0
--model-author-bag-first-weight=1.0

--model-author-bag-middle-initial-weight=8.0
--model-author-bag-middle-noname-penalty=$middle_no_name_penalty
--model-author-bag-middle-name-weight=$middle_author_weight
--model-author-bag-middle-saturation=40.0
--model-author-bag-middle-weight=1.0

--model-author-bag-topics-weight=10.0
--model-author-bag-topics-shift=-0.25
--model-author-bag-topics-entropy=0.0
--model-author-bag-topics-prior=0.0

--model-author-bag-coauthors-weight=0.0
--model-author-bag-coauthors-shift=0.0
--model-author-bag-coauthors-entropy=0.0
--model-author-bag-coauthors-prior=0.0

--model-author-bag-venues-weight=0.0
--model-author-bag-venues-shift=0.0
--model-author-bag-venues-entropy=0.0
--model-author-bag-venues-prior=0.0

--model-author-bag-keywords-weight=0.0
--model-author-bag-keywords-shift=0.0
--model-author-bag-keywords-entropy=0.0
--model-author-bag-keywords-prior=0.0

--model-author-bag-coinventors-weight=$coinventors_locations_assignees_weight
--model-author-bag-coinventors-shift=-0.2
--model-author-bag-coinventors-entropy=0.125
--model-author-bag-coinventors-prior=0.5

--model-author-bag-locations-weight=$coinventors_locations_assignees_weight
--model-author-bag-locations-shift=-0.1
--model-author-bag-locations-entropy=0.25
--model-author-bag-locations-prior=0.5

--model-author-bag-assignees-weight=$coinventors_locations_assignees_weight
--model-author-bag-assignees-shift=-0.1
--model-author-bag-assignees-entropy=0.25
--model-author-bag-assignees-prior=0.5

--model-author-bag-lawyers-weight=$lawyers_weight
--model-author-bag-lawyers-shift=0.0
--model-author-bag-lawyers-entropy=0.0
--model-author-bag-lawyers-prior=0.0

--model-author-bag-cpc-weight=$classification_weight
--model-author-bag-cpc-shift=0.0
--model-author-bag-cpc-entropy=0.0
--model-author-bag-cpc-prior=0.0

--model-author-bag-ipcr-weight=$classification_weight
--model-author-bag-ipcr-shift=0.0
--model-author-bag-ipcr-entropy=0.0
--model-author-bag-ipcr-prior=0.0

--model-author-bag-uspc-weight=$classification_weight
--model-author-bag-uspc-shift=0.0
--model-author-bag-uspc-entropy=0.0
--model-author-bag-uspc-prior=0.0

--model-author-bag-nber-weight=$classification_weight
--model-author-bag-nber-shift=0.0
--model-author-bag-nber-entropy=0.0
--model-author-bag-nber-prior=0.0" > $output_dir/settings_${setting_no}

                done
            done
        done
    done
done