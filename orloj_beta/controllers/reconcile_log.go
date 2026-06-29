package controllers

import (
	"log"

	"github.com/OrlojHQ/orloj/store"
)

func logReconcileError(logger *log.Logger, message string, err error) {
	if logger == nil || err == nil || store.IsConflict(err) {
		return
	}
	logger.Printf("%s: %v", message, err)
}
