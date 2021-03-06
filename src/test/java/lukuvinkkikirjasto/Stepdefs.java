package lukuvinkkikirjasto;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.sql.SQLException;
import java.util.ArrayList;
import lukuvinkkikirjasto.dao.Database;
import lukuvinkkikirjasto.domain.ReadingTip;
import lukuvinkkikirjasto.domain.ReadingTipService;
import lukuvinkkikirjasto.ui.SystemIO;
import lukuvinkkikirjasto.ui.UserInterface;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Stepdefs {

    SystemIO io;
    Database fakeDatabase;
    ReadingTipService rtService;
    UserInterface ui;
    ArrayList<ReadingTip> list;

    public Stepdefs() throws SQLException {
        io = mock(SystemIO.class);
        fakeDatabase = mock(Database.class);
        rtService = new ReadingTipService(fakeDatabase);
        ui = new UserInterface(io, rtService);
        list = new ArrayList<>();
    }

    @Given("command {string} is selected")
    public void commandIsSelected(String command) {
        ui.chooseCommand(command).execute();
    }

    @Given("command {string} is selected and option {string} is given")
    public void commandIsSelected(String command, String option) {
        when(io.input()).thenReturn(option);
        ui.chooseCommand(command).execute();
    }

    @Then("message {string} is shown")
    public void messageIsShown(String message) {
        verify(io).output(message);
    }

    @When("header {string} and description {string} are given")
    public void headerAndDescriptionAreGiven(String header, String description) throws SQLException {
        rtService.add(header, description);
    }

    @Then("the book {string} is added.")
    public void theBookIsAdded(String header) throws SQLException {
        verify(fakeDatabase).create(eq(header), anyString());
    }

    @Then("the tip with id {int} is removed.")
    public void theTipIsRemoved(int id) throws SQLException {
        verify(fakeDatabase).delete(id);
    }

    @Then("the tip with id {int} is not removed.")
    public void theTipsIsNotRemoved(int id) throws SQLException {
        verify(fakeDatabase, times(0)).delete(id);
    }

    @Given("tip with header {string} and description {string} is added")
    public void tipWithHeaderAndDescriptionIsAdded(String header, String desc) throws SQLException {
        rtService.add(header, desc);
        verify(fakeDatabase).create(header, desc);
        list.add(new ReadingTip(list.size()+1, header, desc));
        when(fakeDatabase.getTips()).thenReturn(list);
    }

    @Given("reading tip with id {int} is marked as read")
    public void markReadingTipAsRead(int id) throws SQLException {
        rtService.setReadStatus(id, true);
        verify(fakeDatabase).setReadStatus(id, true);
    }
    
    @Given("reading tip with id {int} is marked as unread")
    public void markReadingTipAsUnread(int id) throws SQLException {
        rtService.setReadStatus(id, false);
        verify(fakeDatabase).setReadStatus(id, false);
    }

    @Then("tip with id, header {string} and description {string} is listed")
    public void tipWithIdAndHeaderAndDescriptionIsListed(String header, String desc) throws SQLException {
        assertTrue(readingTipExistsInList(fakeDatabase.getTips(), header, desc));

        verify(io, times(2)).output("Which tips to list? Type unread/read (default: all)" 
        + "ID: " + anyString() + "\n" + "Header: " + header + "\n" + "Description: " + desc + "\n");
    }

    private boolean readingTipExistsInList(ArrayList<ReadingTip> tips, String header, String desc) {
        for (ReadingTip tip : tips) {
            if (tip.getHeader().equals(header) && tip.getDescription().equals(desc)) {
                return true;
            }
        }
        return false;
    }

    @When("id {int} and new header {string} are given")
    public void idAndNewHeaderAreGiven(int id, String header) throws SQLException {
        rtService.editHeader(id, header);
    }

    @Then("tip with id {int} has its header changed to {string}")
    public void tipWithIdHasItsHeaderChanged(int id, String header) throws SQLException {
        verify(fakeDatabase).editHeader(eq(id), eq(header));
    }

    @When("id {int} and new description {string} are given")
    public void idAndNewDescriptionAreGiven(int id, String description) throws SQLException {
        rtService.editDescription(id, description);
    }

    @When ("delete id {string} is given")
    public void deleteIdIsGiven(String id) {
        when(io.input()).thenReturn(id);
    }

    @When("delete id {int} and confirmation {string} are given")
    public void deleteIdAndConfirmationAreGiven(int id, String confirmation) throws SQLException {
        if (confirmation.equals("y")) {
            rtService.delete(id);
        }
    }

    @When("confirmation {string} is given")
    public void confirmationIsGiven(String confirmation) throws SQLException {
        when(io.input()).thenReturn(confirmation);
    }

    @Then("tip with id {int} has its description changed to {string}")
    public void tipdWithIdHasItsDescriptionChanged(int id, String description) throws SQLException {
        verify(fakeDatabase).editDescription(id, description);
    }

    @Given("input {string} will be given") 
    public void inputGiven(String input) {
        when(io.input()).thenReturn(input); 
    }

    @Then("header editor is not accessed")
    public void headerEditorNotAccessed() throws SQLException {
        verify(fakeDatabase, times(0)).editHeader(anyInt(), anyString());
    }

    @When("search term {string} is entered")
    public void searchTermIsEntered(String searchTerm) throws SQLException {
        when(io.input()).thenReturn(searchTerm);
        rtService.searchReadingTips(searchTerm);
    }

    @Then("a search is performed with the search term {string}")
    public void searchIsPerformed(String searchTerm) throws SQLException {
        verify(fakeDatabase, times(1)).searchFromTips(searchTerm);
    }

    @Given("the database contains 2 reading tips that match the search term: {string}, {string} and {string}, {string}")
    public void matchesFoundInDatabase(String header1, String desc1, String header2, String desc2) throws SQLException {
        ArrayList<ReadingTip> searchResults = new ArrayList<>();
        searchResults.add(new ReadingTip(3, header1, desc1, false));
        searchResults.add(new ReadingTip(7, header2, desc2, false));
        when(rtService.searchReadingTips(anyString())).thenReturn(searchResults);
    }

    @Then("reading tip with header {string} and description {string} is included in the search results")
    public void tipIsIncludedInSearchResults(String header, String description) throws SQLException {
        ArrayList<ReadingTip> searchResults = rtService.searchReadingTips(anyString());
        assertTrue(readingTipExistsInList(searchResults, header, description));
    }

    @Given("the database does not contain any reading tips that match the search term")
    public void noMatchesInDatabase() throws SQLException {
        when(rtService.searchReadingTips(anyString())).thenReturn(new ArrayList<ReadingTip>());
    }
}
