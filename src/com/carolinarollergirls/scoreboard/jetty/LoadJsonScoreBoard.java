package com.carolinarollergirls.scoreboard.jetty;
/**
 * Copyright (C) 2008-2012 Mr Temper <MrTemper@CarolinaRollergirls.com>
 *
 * This file is part of the Carolina Rollergirls (CRG) ScoreBoard.
 * The CRG ScoreBoard is licensed under either the GNU General Public
 * License version 3 (or later), or the Apache License 2.0, at your option.
 * See the file COPYING for details.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.fasterxml.jackson.jr.ob.JSON;

import com.carolinarollergirls.scoreboard.core.interfaces.ScoreBoard;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider.Source;
import com.carolinarollergirls.scoreboard.json.ScoreBoardJSONSetter;
import com.carolinarollergirls.scoreboard.utils.StatsbookImporter;

public class LoadJsonScoreBoard extends HttpServlet {
    public LoadJsonScoreBoard(ScoreBoard sb) {
        this.scoreBoard = sb;
        sbImporter = new StatsbookImporter(sb);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (scoreBoard.getClients().getDevice(request.getSession().getId()).mayWrite()) {
            scoreBoard.getClients().getDevice(request.getSession().getId()).write();
            try {
                if (!ServletFileUpload.isMultipartContent(request)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                ServletFileUpload sfU = new ServletFileUpload();
                FileItemIterator items = sfU.getItemIterator(request);
                while (items.hasNext()) {
                    FileItemStream item = items.next();
                    if (!item.isFormField()) {
                        if (request.getPathInfo().equalsIgnoreCase("/JSON")) {
                            InputStream stream = item.openStream();
                            Map<String, Object> map = JSON.std.mapFrom(stream);
                            stream.close();
                            scoreBoard.runInBatch(new Runnable() {
                                @Override
                                @SuppressWarnings("unchecked")
                                public void run() {
                                    ScoreBoardJSONSetter.set(scoreBoard, (Map<String, Object>) map.get("state"),
                                                             Source.JSON);
                                }
                            });
                            response.setContentType("text/plain");
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else if (request.getPathInfo().equalsIgnoreCase("/xlsx")) {
                            sbImporter.read(item.openStream());
                        }
                        return;
                    }
                }
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No File uploaded");
            } catch (FileUploadException fuE) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, fuE.getMessage());
            } catch (IOException iE) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error Reading File: " + iE.getMessage());
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No write access");
        }
    }

    protected final ScoreBoard scoreBoard;
    protected final StatsbookImporter sbImporter;
}
