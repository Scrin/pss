package org.assembly.pss.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.assembly.pss.annotation.RequireAdmin;
import org.assembly.pss.bean.ImportResult;
import org.assembly.pss.bean.persistence.entity.Event;
import org.assembly.pss.bean.persistence.entity.Location;
import org.assembly.pss.bean.persistence.entity.Tag;
import org.assembly.pss.database.Database;
import org.assembly.pss.service.CsvService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequireAdmin
@Api(tags = "Admin")
@RestController
@RequestMapping("/api/admin")
public class Admin extends AbstractController {

    @Resource
    private Database database;
    @Resource
    private CsvService csvService;

    @RequestMapping(method = RequestMethod.GET, value = "/event/party/{party}")
    @ApiOperation(value = "Get all public and non-public events for a given party", authorizations = {
        @Authorization(value = "basicAuth")})
    public List<Event> getEvents(@PathVariable String party) {
        return database.getEvents(party);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/location")
    @ApiOperation(value = "Get all (event) locations", authorizations = {
        @Authorization(value = "basicAuth")})
    public List<Location> getLocations() {
        return database.getLocations();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tag")
    @ApiOperation(value = "Get all (event) tags", authorizations = {
        @Authorization(value = "basicAuth")})
    public List<Tag> getTags() {
        return database.getTags();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/party")
    @ApiOperation(value = "Get all parties that currently have at least one event",
            notes = "Parties don't actually exist in the database, therefore if at least one event has a party, then that party exists. "
            + "If the last event for a party is deleted, the party gets deleted as well.",
            authorizations = {
                @Authorization(value = "basicAuth")})
    public List<String> getParties() {
        return database.getParties();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/event")
    @ApiOperation(value = "Create or Update an event", notes = "If an event exists with the provided ID, then that event is updated with the given data. "
            + "If no ID is given, or the ID does not exist in the database, then a new event is created and a new ID is assigned to it, ignoring the supplied ID. "
            + "The response contains the event with the created or updated data, including the ID that was eventually assigned to it if it was just created. \n"
            + "Similar ID behavior applies to the provided Location and Tags; if an ID for the supplied location or tag exists, "
            + "then the existing entry is used and the supplied location/tag data is *ignored* (apart from the the ID(s)). "
            + "If it does not, a new location/tag is created with an automatically assigned ID and the supplied data. \n"
            + "This endpoint cannot be used to modify existing locations or tags, you can only modify *which* locations or tags the event has.",
            authorizations = {
                @Authorization(value = "basicAuth")})
    public Event createOrUpdateEvent(@RequestBody Event event) {
        return database.merge(event);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/location")
    @ApiOperation(value = "Create or Update a location", notes = "If a location exists with the provided ID, then that location is updated with the given data. "
            + "If no ID is given, or the ID does not exist in the database, then a new location is created and a new ID is assigned to it, ignoring the given ID. "
            + "The response contains the location with the created or updated data, including the ID that was eventually assigned to it if it was just created.",
            authorizations = {
                @Authorization(value = "basicAuth")})
    public Location createOrUpdateLocation(@RequestBody Location location) {
        return database.merge(location);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tag")
    @ApiOperation(value = "Create or Update a tag", notes = "If a tag exists with the provided ID, then that tag is updated with the given data. "
            + "If no ID is given, or the ID does not exist in the database, then a new tag is created and a new ID is assigned to it, ignoring the given ID. "
            + "The response contains the tag with the created or updated data, including the ID that was eventually assigned to it if it was just created.",
            authorizations = {
                @Authorization(value = "basicAuth")})
    public Tag createOrUpdateTag(@RequestBody Tag tag) {
        return database.merge(tag);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/event/id/{id}")
    @ApiOperation(value = "Delete an event", authorizations = {
        @Authorization(value = "basicAuth")})
    public void deleteEvent(@PathVariable Integer id) {
        Event event = database.getEvent(id);
        if (event == null) {
            throw new IllegalStateException("Can't delete event with ID: " + id + " since it doesn't seem to exist");
        }
        database.remove(event);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/location/id/{id}")
    @ApiOperation(value = "Delete a location", authorizations = {
        @Authorization(value = "basicAuth")})
    public void deleteLocation(@PathVariable Integer id) {
        Location location = database.getLocation(id);
        if (location == null) {
            throw new IllegalStateException("Can't delete location with ID: " + id + " since it doesn't seem to exist");
        }
        database.remove(location);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/tag/id/{id}")
    @ApiOperation(value = "Delete a tag", authorizations = {
        @Authorization(value = "basicAuth")})
    public void deleteTag(@PathVariable Integer id) {
        Tag tag = database.getTag(id);
        if (tag == null) {
            throw new IllegalStateException("Can't delete tag with ID: " + id + " since it doesn't seem to exist");
        }
        database.remove(tag);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/event/import")
    @ApiOperation(value = "Import events from a CSV file", authorizations = {
        @Authorization(value = "basicAuth")})
    public ImportResult importEvents(@RequestBody byte[] file,
            @ApiParam("Force dangerous operations such as changing the party of existing events") @RequestParam(required = false) Boolean force) {
        return csvService.importEvents(new ByteArrayInputStream(file), Boolean.TRUE.equals(force));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/event/party/{name}/export")
    @ApiOperation(value = "Export events to a CSV file", produces = "text/csv", authorizations = {
        @Authorization(value = "basicAuth")})
    public String exportEvents(HttpServletResponse response, @PathVariable String name) {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"events_" + name + ".csv\"");
        StringWriter writer = new StringWriter();
        csvService.exportEvents(writer, name);
        return writer.toString();
    }
}
