package com.mrdeveloper.mytourplan.models;

import java.util.List;

public class TripNotesResponse {
    private boolean success;
    private String error;
    private List<TripNote> notes;
    private List<TripChecklistItem> checklist;

    public TripNotesResponse() {}

    public TripNotesResponse(boolean success, String error, List<TripNote> notes, List<TripChecklistItem> checklist) {
        this.success = success;
        this.error = error;
        this.notes = notes;
        this.checklist = checklist;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<TripNote> getNotes() { return notes; }
    public void setNotes(List<TripNote> notes) { this.notes = notes; }

    public List<TripChecklistItem> getChecklist() { return checklist; }
    public void setChecklist(List<TripChecklistItem> checklist) { this.checklist = checklist; }
}
