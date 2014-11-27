# IHTSDO DrugMatch Rules

##"Check"

**AMBIGUOUS_MATCH** - more than one term matched within the confined hierarchy.

**CASE_INSENSITIVE_MATCH** - one term matched the input, except for the character case.

**COMPONENT_AND_TERM_MISMATCH** - an error has occurred.

**EXACT_MATCH** - one term matched the input exactly.

**TRANSLATION_MISSING** - no national term matched the input.

**UNCHECKED** - English input "Check" skipped, because the national input was successfully unambiguously matched.

**ZERO_MATCH** - zero concepts matched the input.


##"Match" attribute

**AMBIGUOUS_MATCH** - more than one concept matched the input attributes.

**AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM** - more than one concept matched the input attributes, while excluding dose form.

**EXACT_MATCH** - one concept matched the input attributes exactly.

**EXACT_MATCH_EXCLUDING_DOSE_FORM** - one concept matched the input attributes exactly, while excluding dose form.

**DOSE_FORM_MISSING_CHECK_CONCEPT** - "Match" attribute skipped, because the attribute substance target didn't "Check" out.

**SUBSTANCE_MISSING_CHECK_CONCEPT** - "Match" attribute skipped, because the attribute dose form target didn't "Check" out.

**ZERO_MATCH** - zero concept attributes matched the input, both with and without dose form.

	
##"Match" term

The rule order below reflects the "strength" of match(es), only the rule with the highest "strength" is reported.

The "Rule match" column might include a "AMBIGUOUS_" prefix when a rule was triggered more than once for the same input.

**PHARMACEUTICAL_EXACT_NATIONAL_MATCH** - Trade name, component(s) & dose form all matched a national term successfully.

**PHARMACEUTICAL_CASE_INSENSITIVE_NATIONAL_MATCH** - Trade name, component(s) & dose form all matched a national term successfully, except for the character case.

**PHARMACEUTICAL_INCORRECT_COMPONENT_ORDER_NATIONAL** - Trade name, component(s) & dose form all matched a national term successfully, except for the component order.

**GENERIC_EXACT_NATIONAL_MATCH** - Component(s) & dose form all matched a national term successfully.

**GENERIC_EXACT_ENGLISH_MATCH** - Component(s) & dose form all matched a English term successfully.

**GENERIC_CASE_INSENSITIVE_NATIONAL_MATCH** - Component(s) & dose form all matched a national term successfully, except for the character case.

**GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH** - Component(s) & dose form all matched a English term successfully, except for the character case.

**GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL** - Component(s) & dose form all matched a national term successfully, except for the component order.

**PHARMACEUTICAL_PARTIAL_TRADE_NAME_NATIONAL** - Component(s) & dose form all matched a national term successfully, except for only a partial match on the input trade name.

**GENERIC_PARTIAL_NATIONAL_DOSE_FORM** - Component(s) all matched a national term successfully, except for only a partial match on the input dose form.

**GENERIC_MISSING_NATIONAL_SUBSTANCE** - Substance was unmatched in a national term.

**GENERIC_MISSING_NATIONAL_DOSE_FORM** - Dose form was unmatched in a national term.

**GENERIC_MISSING_NATIONAL_UNIT** - Unit was unmatched in a national term.

**GENERIC_MISSING_NATIONAL_STRENGTH** - Strength was unmatched in a national term.

**GENERIC_INCORRECT_COMPONENT_ORDER_ENGLISH** - Component(s) & dose form all matched a English term successfully, except for the component order.

**GENERIC_PARTIAL_ENGLISH_DOSE_FORM** - Component(s) all matched a English term successfully, except for only a partial match on the input dose form.

**GENERIC_MISSING_ENGLISH_SUBSTANCE** - Substance was unmatched in a English term.

**GENERIC_MISSING_ENGLISH_DOSE_FORM** - Dose form was unmatched in a English term.

**GENERIC_MISSING_ENGLISH_UNIT** - Unit was unmatched in a English term.

**GENERIC_MISSING_ENGLISH_STRENGTH** - Strength was unmatched in a English term.

**ZERO_TERM_MATCH** - zero concepts matched the input.

**ZERO_ATTRIBUTE_MATCH** - zero concept attributes matched the input, both with and without dose form.

