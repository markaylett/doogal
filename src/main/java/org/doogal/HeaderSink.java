package org.doogal;
import org.apache.maven.doxia.logging.Log;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;

/**
 * <pre>
 *   sink.head();
 *
 *   sink.title();
 *   sink.text( "Title" );
 *   sink.title_();
 *
 *   sink.author();
 *   sink.text( "Author" );
 *   sink.author_();
 *
 *   sink.date();
 *   sink.text( "Date" );
 *   sink.date_();
 *
 *   sink.head_();
 * </pre>
 */

final class HeaderSink implements Sink {

	private final Sink sink;

	HeaderSink(Sink sink) {
		this.sink = sink;
	}

	public final void anchor_() {
		sink.anchor_();
	}

	public final void anchor(String name, SinkEventAttributes attributes) {
		sink.anchor(name, attributes);
	}

	public final void anchor(String name) {
		sink.anchor(name);
	}

	public final void author_() {
		sink.author_();
	}

	public final void author() {
		sink.author();
	}

	public final void author(SinkEventAttributes attributes) {
		sink.author(attributes);
	}

	public final void body_() {
		sink.body_();
	}

	public final void body() {
		sink.body();
	}

	public final void body(SinkEventAttributes attributes) {
		sink.body(attributes);
	}

	public final void bold_() {
		sink.bold_();
	}

	public final void bold() {
		sink.bold();
	}

	public final void close() {
		sink.close();
	}

	public final void comment(String comment) {
		sink.comment(comment);
	}

	public final void date_() {
		sink.date_();
	}

	public final void date() {
		sink.date();
	}

	public final void date(SinkEventAttributes attributes) {
		sink.date(attributes);
	}

	public final void definedTerm_() {
		sink.definedTerm_();
	}

	public final void definedTerm() {
		sink.definedTerm();
	}

	public final void definedTerm(SinkEventAttributes attributes) {
		sink.definedTerm(attributes);
	}

	public final void definition_() {
		sink.definition_();
	}

	public final void definition() {
		sink.definition();
	}

	public final void definition(SinkEventAttributes attributes) {
		sink.definition(attributes);
	}

	public final void definitionList_() {
		sink.definitionList_();
	}

	public final void definitionList() {
		sink.definitionList();
	}

	public final void definitionList(SinkEventAttributes attributes) {
		sink.definitionList(attributes);
	}

	public final void definitionListItem_() {
		sink.definitionListItem_();
	}

	public final void definitionListItem() {
		sink.definitionListItem();
	}

	public final void definitionListItem(SinkEventAttributes attributes) {
		sink.definitionListItem(attributes);
	}

	public final void enableLogging(Log log) {
		sink.enableLogging(log);
	}

	public final void figure_() {
		sink.figure_();
	}

	public final void figure() {
		sink.figure();
	}

	public final void figure(SinkEventAttributes attributes) {
		sink.figure(attributes);
	}

	public final void figureCaption_() {
		sink.figureCaption_();
	}

	public final void figureCaption() {
		sink.figureCaption();
	}

	public final void figureCaption(SinkEventAttributes attributes) {
		sink.figureCaption(attributes);
	}

	public final void figureGraphics(String src, SinkEventAttributes attributes) {
		sink.figureGraphics(src, attributes);
	}

	public final void figureGraphics(String name) {
		sink.figureGraphics(name);
	}

	public final void flush() {
		sink.flush();
	}

	public final void head_() {
		sink.head_();
	}

	public final void head() {
		sink.head();

		sink.title();
		sink.text("Test Title");
		sink.title_();

		sink.author();
		sink.text("Toby Aylett");
		sink.author_();

		sink.author();
		sink.text("Emily Aylett");
		sink.author_();
		
		sink.date();
		sink.text("19-Sep-09");
		sink.date_();
	}

	public final void head(SinkEventAttributes attributes) {
		sink.head(attributes);
	}

	public final void horizontalRule() {
		sink.horizontalRule();
	}

	public final void horizontalRule(SinkEventAttributes attributes) {
		sink.horizontalRule(attributes);
	}

	public final void italic_() {
		sink.italic_();
	}

	public final void italic() {
		sink.italic();
	}

	public final void lineBreak() {
		sink.lineBreak();
	}

	public final void lineBreak(SinkEventAttributes attributes) {
		sink.lineBreak(attributes);
	}

	public final void link_() {
		sink.link_();
	}

	public final void link(String name, SinkEventAttributes attributes) {
		sink.link(name, attributes);
	}

	public final void link(String name) {
		sink.link(name);
	}

	public final void list_() {
		sink.list_();
	}

	public final void list() {
		sink.list();
	}

	public final void list(SinkEventAttributes attributes) {
		sink.list(attributes);
	}

	public final void listItem_() {
		sink.listItem_();
	}

	public final void listItem() {
		sink.listItem();
	}

	public final void listItem(SinkEventAttributes attributes) {
		sink.listItem(attributes);
	}

	public final void monospaced_() {
		sink.monospaced_();
	}

	public final void monospaced() {
		sink.monospaced();
	}

	public final void nonBreakingSpace() {
		sink.nonBreakingSpace();
	}

	public final void numberedList_() {
		sink.numberedList_();
	}

	public final void numberedList(int numbering, SinkEventAttributes attributes) {
		sink.numberedList(numbering, attributes);
	}

	public final void numberedList(int numbering) {
		sink.numberedList(numbering);
	}

	public final void numberedListItem_() {
		sink.numberedListItem_();
	}

	public final void numberedListItem() {
		sink.numberedListItem();
	}

	public final void numberedListItem(SinkEventAttributes attributes) {
		sink.numberedListItem(attributes);
	}

	public final void pageBreak() {
		sink.pageBreak();
	}

	public final void paragraph_() {
		sink.paragraph_();
	}

	public final void paragraph() {
		sink.paragraph();
	}

	public final void paragraph(SinkEventAttributes attributes) {
		sink.paragraph(attributes);
	}

	public final void rawText(String text) {
		sink.rawText(text);
	}

	public final void section_(int level) {
		sink.section_(level);
	}

	public final void section(int level, SinkEventAttributes attributes) {
		sink.section(level, attributes);
	}

	public final void section1_() {
		sink.section1_();
	}

	public final void section1() {
		sink.section1();
	}

	public final void section2_() {
		sink.section2_();
	}

	public final void section2() {
		sink.section2();
	}

	public final void section3_() {
		sink.section3_();
	}

	public final void section3() {
		sink.section3();
	}

	public final void section4_() {
		sink.section4_();
	}

	public final void section4() {
		sink.section4();
	}

	public final void section5_() {
		sink.section5_();
	}

	public final void section5() {
		sink.section5();
	}

	public final void sectionTitle_() {
		sink.sectionTitle_();
	}

	public final void sectionTitle_(int level) {
		sink.sectionTitle_(level);
	}

	public final void sectionTitle() {
		sink.sectionTitle();
	}

	public final void sectionTitle(int level, SinkEventAttributes attributes) {
		sink.sectionTitle(level, attributes);
	}

	public final void sectionTitle1_() {
		sink.sectionTitle1_();
	}

	public final void sectionTitle1() {
		sink.sectionTitle1();
	}

	public final void sectionTitle2_() {
		sink.sectionTitle2_();
	}

	public final void sectionTitle2() {
		sink.sectionTitle2();
	}

	public final void sectionTitle3_() {
		sink.sectionTitle3_();
	}

	public final void sectionTitle3() {
		sink.sectionTitle3();
	}

	public final void sectionTitle4_() {
		sink.sectionTitle4_();
	}

	public final void sectionTitle4() {
		sink.sectionTitle4();
	}

	public final void sectionTitle5_() {
		sink.sectionTitle5_();
	}

	public final void sectionTitle5() {
		sink.sectionTitle5();
	}

	public final void table_() {
		sink.table_();
	}

	public final void table() {
		sink.table();
	}

	public final void table(SinkEventAttributes attributes) {
		sink.table(attributes);
	}

	public final void tableCaption_() {
		sink.tableCaption_();
	}

	public final void tableCaption() {
		sink.tableCaption();
	}

	public final void tableCaption(SinkEventAttributes attributes) {
		sink.tableCaption(attributes);
	}

	public final void tableCell_() {
		sink.tableCell_();
	}

	public final void tableCell() {
		sink.tableCell();
	}

	public final void tableCell(SinkEventAttributes attributes) {
		sink.tableCell(attributes);
	}

	@Deprecated
	public final void tableCell(String width) {
		sink.tableCell(width);
	}

	public final void tableHeaderCell_() {
		sink.tableHeaderCell_();
	}

	public final void tableHeaderCell() {
		sink.tableHeaderCell();
	}

	public final void tableHeaderCell(SinkEventAttributes attributes) {
		sink.tableHeaderCell(attributes);
	}

	@Deprecated
	public final void tableHeaderCell(String width) {
		sink.tableHeaderCell(width);
	}

	public final void tableRow_() {
		sink.tableRow_();
	}

	public final void tableRow() {
		sink.tableRow();
	}

	public final void tableRow(SinkEventAttributes attributes) {
		sink.tableRow(attributes);
	}

	public final void tableRows_() {
		sink.tableRows_();
	}

	public final void tableRows(int[] justification, boolean grid) {
		sink.tableRows(justification, grid);
	}

	public final void text(String text, SinkEventAttributes attributes) {
		sink.text(text, attributes);
	}

	public final void text(String text) {
		sink.text(text);
	}

	public final void title_() {
		sink.title_();
	}

	public final void title() {
		sink.title();
	}

	public final void title(SinkEventAttributes attributes) {
		sink.title(attributes);
	}

	public final void unknown(String name, Object[] requiredParams,
			SinkEventAttributes attributes) {
		sink.unknown(name, requiredParams, attributes);
	}

	public final void verbatim_() {
		sink.verbatim_();
	}

	@Deprecated
	public final void verbatim(boolean boxed) {
		sink.verbatim(boxed);
	}

	public final void verbatim(SinkEventAttributes attributes) {
		sink.verbatim(attributes);
	}
}
