import { pool } from "../db.js";

export interface PublicUser {
    id: number;
    full_name: string;
    email: string;
}

interface AuthUser extends PublicUser {
    password_hash: string;
}

interface Profile extends PublicUser {
    created_at: Date | null;
}

export async function findUserByEmail(email: string) {
    const result = await pool.query<{ id: number }>(
        "SELECT id FROM users WHERE lower(email) = $1 LIMIT 1",
        [email]
    );
    return result.rows[0] ?? null;
}

export async function createUser(fullName: string, email: string, passwordHash: string) {
    const result = await pool.query<PublicUser>(
        `INSERT INTO users (full_name, email, password_hash)
         VALUES ($1, $2, $3)
         ON CONFLICT (email) DO NOTHING
         RETURNING id, full_name, email`,
        [fullName, email, passwordHash]
    );
    return result.rows[0] ?? null;
}

export async function findUserForLogin(email: string) {
    const result = await pool.query<AuthUser>(
        `SELECT id, full_name, email, password_hash
         FROM users
         WHERE lower(email) = $1
         ORDER BY (email = $1) DESC, id
         LIMIT 1`,
        [email]
    );
    return result.rows[0] ?? null;
}

export async function findProfileById(id: number) {
    const result = await pool.query<Profile>(
        "SELECT id, full_name, email, created_at FROM users WHERE id = $1",
        [id]
    );
    return result.rows[0] ?? null;
}
