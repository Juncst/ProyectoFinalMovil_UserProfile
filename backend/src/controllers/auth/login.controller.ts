import type { Request, Response } from "express";
import { loginUser } from "../../services/auth/login.service.js";

export async function login(req: Request, res: Response) {
    const { email, password } = req.body ?? {};
    if (typeof email !== "string" || typeof password !== "string" || !email.trim() || !password) {
        return res.status(400).json({ message: "Correo y contraseña son obligatorios" });
    }

    const result = await loginUser(email.trim().toLowerCase(), password);
    if (!result) {
        return res.status(401).json({ message: "Correo o contraseña incorrectos" });
    }

    return res.status(200).json({ message: "Inicio de sesión exitoso", ...result });
}
