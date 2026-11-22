import pg from "pg";
import { config } from "./config.js";

export const pool = new pg.Pool({
    ...config.database,
    connectionTimeoutMillis: 5000,
});

pool.on("error", () => {
    console.error("Se perdió una conexión inactiva con PostgreSQL");
});
