import type { Request, Response } from "express";
import { findProfileById } from "../repositories/user.repository.js";

export async function getProfile(_req: Request, res: Response) {
    const profile = await findProfileById(res.locals.userId);
    if (!profile) {
        return res.status(404).json({ message: "Usuario no encontrado" });
    }

    return res.json(profile);
}
