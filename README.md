# IHTSDO DrugMatch

Tool preparing missing pharmaceutical content, both national and generic, for import into SNOMED CT (a.k.a. SCT).

## Getting started

### Prerequisites
* download ex. ihtsdo-sct-drugmatch-1.0-distributable.zip
* [Java](http://www.java.com/) 7 (or compatible) runtime.
* [HealthTerm™](http://www.healthterm.com/) v8.0 (or newer) access, with [SCT](http://www.ihtsdo.org/snomed-ct/) preloaded.

### Input file (UTF-8)

Example:

    example/drugmatch_input_example.csv

Example data is intended to cover the most common use cases, and may be subject to change.

### Configuration

#### Settings

The behavior of the tool is controlled via:

    setting.properties

<table border="1">
	<thead>
		<tr>
			<th>Setting</th>
			<th>Value</th>
			<th>Description</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>input.file</td>
			<td>path</td>
			<td>Input file</td>
		</tr>
		<tr>
			<td>input.file.include_first_line</td>
			<td>boolean</td>
			<td>Set to true if first line contains data and should be included for processing (default: false)</td>
		</tr>
		<tr>
			<td>file.content.separator_character</td>
			<td>character</td>
			<td>Column separator character (default: ;)</td>
		</tr>
		<tr>
			<td>file.content.quote_character</td>
			<td>character</td>
			<td>Column value quote character (optional)</td>
		</tr>
		<tr>
			<td>sct.extension.language_code</td>
			<td>Ex. 'en'</td>
			<td>National language code (ISO 639-1)</td>
		</tr>
		<tr>
			<td>sct.extension.namespace_id</td>
			<td>Namespace ID</td>
			<td>National SCT extension namespace ID</td>
		</tr>
		<tr>
			<td>sct.id.service</td>
			<td>URL</td>
			<td>SCT ID service URL</td>
		</tr>
		<tr>
			<td>sct.module_id</td>
			<td></td>
			<td>
				<a href="http://www.snomed.org/tig?t=trg2main_gen_idsource">Source Module ID</a>
			</td>
		</tr>
		<tr>
			<td>sct.extension.language_reference_set_id</td>
			<td></td>
			<td>
				<a href="http://www.snomed.org/tig?t=trg2rfs_spec_lang">Language Reference Set ID</a>
			</td>
		</tr>
		<tr>
			<td>sct.extension.quantity_reference_set_id</td>
			<td></td>
			<td>
				<a href="http://www.snomed.org/tig?t=trg2rfs_spec_overview">Quantity Reference Set ID</a>
			</td>
		</tr>
		<tr>
			<td>sct.release_id</td>
			<td>Ex. '201401031'</td>
			<td>SCT release ID</td>
		</tr>
		<tr>
			<td>verification.login</td>
			<td>username</td>
			<td>Verification service username</td>
		</tr>
		<tr>
			<td>verification.password</td>
			<td>password</td>
			<td>Verification service password</td>
		</tr>
		<tr>
			<td>verification.service</td>
			<td>URL</td>
			<td>Verification service URL</td>
		</tr>
		<tr>
			<td>output.dir</td>
			<td>path</td>
			<td>Output folder (ex. './result' is equal to execution directory/result)</td>
		</tr>
		<tr>
			<td>generic_report</td>
			<td>boolean</td>
			<td>If set to true, reports will contain generic rule messages (default: false)</td>
		</tr>
		<tr>
			<td>strict_mode</td>
			<td>boolean</td>
			<td>If set to true, "Check" warnings will be treated as errors (default: false)</td>
		</tr>
		<tr>
			<td>sct.attribute_id.has_active_ingredient</td>
			<td>Concept ID</td>
			<td>"Match" attribute, Has active ingredient</td>
		</tr>
		<tr>
			<td>sct.attribute_id.has_dose_form</td>
			<td>Concept ID</td>
			<td>"Match" attribute, Has dose form</td>
		</tr>
		<tr>
			<td>sct.constraint_id.dose_form</td>
			<td>Concept ID</td>
			<td>"Check" dose form top point limitation</td>
		</tr>
		<tr>
			<td>sct.constraint_id.substance</td>
			<td>Concept ID</td>
			<td>"Check" substance top point limitation</td>
		</tr>
		<tr>
			<td>sct.constraint_id.unit</td>
			<td>Concept ID</td>
			<td>"Check" unit top point limitation</td>
		</tr>
	</tbody>
</table>

Each phase in the DrugMatch flow, can be triggered independently using the matching executable (ex. check.bat).

### Execution

#### "Check"

Verify that all pharmaceutical components, ie. substance, dose form and unit of measurement, can be matched with only one exact SCT equivalent representation.

Generates a status report for each of the above mentioned pharmaceutical component attributes.

#### "Match"

Based on successful completion of the previous step, the following approach should be able to match a national pharmaceutical product with only one exact SCT generic pharmaceutical product, if present, based on the product characteristics provided in the input file.

##### "Match" attribute

Retrieve SCT concept matches where the attributes contains the product components, from the product hierarchy.

Optionally generates a status report for this step, by providing runtime argument "--matchAttributeReport", in order to support debugging.

##### "Match" term

Based on successful completion of the previous step, the "Match" term attempts to determine if a pharmaceutical is present in either national or generic SCT concept representation.

Generates a status report for each of the above mentioned pharmaceutical component attributes.

#### "Create" (default)

Generates SCT Release Format 2 for unmatched SCT generic and national product concepts with fully dressed attributes and terms, based on the pharmaceutical products characteristics in the input file.

Also generates a simple 1 <-> 1 mapping file between the input Drug ID and corresponding SCT Concept ID.

## License

[Apache License Version 2.0](LICENSE)

## Build
This tool was developed with [Java](http://www.java.com/) 7 and [Apache Maven](http://maven.apache.org/) v3.2.1.

Build, unit test and generate reports for project, command:

    mvn install site
